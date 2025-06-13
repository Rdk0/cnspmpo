(ns pmpo.read-data
  (:require
   [tablecloth.api :as tc]
   [pmpo.model :as md]))


(def new-params
  "read in the values of ID, MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa. "
  (let [params (tc/dataset ".\\data\\input.csv")]
    (-> params
        (tc/rename-columns
         (into {} (map (fn [col-name] [col-name (keyword col-name)])
                       (tc/column-names params)))))))

(defn calculate-result
  "Calculates the result based on MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa. apply sigmoid correction?"
  [{:keys [MW HBD TPSA cLogD_ACD_v15 mbpKa]} sigmoid-correction?]
  (+ (md/calculate-contribution :mw MW sigmoid-correction?)
     (md/calculate-contribution :hbd HBD sigmoid-correction?)
     (md/calculate-contribution :psa TPSA sigmoid-correction?)
     (md/calculate-contribution :clogd cLogD_ACD_v15 sigmoid-correction?)
     (md/calculate-contribution :pka mbpKa sigmoid-correction?)))

(defn calulated-pmpo-values
  "create df with the calculated pmpo"
  [sigmoid-correction?]
  (let [new-params-small (tc/select-columns new-params [:ID :MW :HBD :TPSA :cLogD_ACD_v15 :mbpKa])
        new (tc/drop-missing new-params-small)
        with-results  (tc/map-rows new
                                   (fn [row]
                                     ;; Call your calculate-result function on the row map
                                     {:cns-mpo-calc (calculate-result row sigmoid-correction?)}));))
        with-results (tc/select-columns with-results [:ID :cns-mpo-calc])]
    (if sigmoid-correction?
      (tc/rename-columns with-results {:cns-mpo-calc :cns-mpo-sigm-calc})
      with-results)))

(defn report-results
  "combine into a df all the calculated values"
  []
  (let [calculated-pmpo-no-sigmoid (calulated-pmpo-values false)
        calculated-pmpo-sigmoid (calulated-pmpo-values true)]
    (tc/left-join  calculated-pmpo-no-sigmoid calculated-pmpo-sigmoid :ID)))

