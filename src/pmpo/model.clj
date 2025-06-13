(ns pmpo.model
  (:require 
   [clojure.math :as m]))

(def coef
  {:clogd  [0.13 1.81 1.93 0.02 131996.99  1.42]
   :hbd [0.27  1.09 0.89 0.09  0.00  1.46]
   :pka [0.12 8.07  2.21  0.02  1459310.78 7.66]
   :mw [0.16  304.70  94.05  0.03  0.83  328.30]
   :psa [0.33  50.70  28.30  0.15  0.79  65.74]})

;;CNS pMPO: 0.13 * np.exp(-1.0 * ([cLogD_ACD_v15] - 1.81)^2 / (2.0 * (1.93)^2)) * np.power(1.0 + 0.02 * np.power(131996.99, -1.0 * ([cLogD_ACD_v15] - 1.42)), -1.0) + 0.27 * np.exp(-1.0 * ([HBD] - 1.09)^2 / (2.0 * (0.89)^2)) * np.power(1.0 + 0.09 * np.power(0.00, -1.0 * ([HBD] - 1.46)), -1.0) + 0.12 * np.exp(-1.0 * ([mbpKa] - 8.07)^2 / (2.0 * (2.21)^2
;; )) * np.power(1.0 + 0.02 * np.power(1459310.78, -1.0 * ([mbpKa] - 7.66)), -1.0) + 0.16 * np.exp(-1.0 * ([MW] - 304.70)^2 / (2.0 * (94.05)^2)) * np.power(1.0 + 0.03 * np.power(0.83, -1.0 * ([MW] - 328.30)), -1.0) + 0.33 * np.exp(-1.0 * ([TPSA] - 50.70)^2 / (2.0 * (28.30)^2)) * np.power(1.0 + 0.15 * np.power(0.79, -1.0 * ([TPSA] - 65.74)), -1.0)  
;;np.power(1.0 + 0.02 * np.power(131996.99, -1.0 * ([cLogD_ACD_v15] - 1.42)), -1.0)


(defn calculate-correction 
  "calculate the correction for one property"
  [d e f value]
   (m/pow
     (+ 1.0 (* d (m/pow e (* -1.0 (- value f))))) -1.0))


(defn calculate-contribution
"calculated a single property contribution to the overall pmpo score 
 with or without the sigmoidal correction"
      [property value apply-correction?]
      (let [a (get (property coef) 0)
            b (get (property coef) 1)
            c (get (property coef) 2)
            d (get (property coef) 3)
            e (get (property coef) 4)
            f (get (property coef) 5) 
            contribution (* a
                            (m/exp
                             (* -1.0 (/ (* (- value b) (- value b))
                                        (* 2.0 c c)))))] 
      (if apply-correction?
                (* contribution ( calculate-correction d e f value))
                contribution)) )
