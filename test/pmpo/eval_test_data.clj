(ns pmpo.eval-test-data
  (:require
   [tablecloth.api :as tc]
   [pmpo.model :as md]))


(defn  get-test-dataset
  "read in the test data set"
  []
  (let [ds (tc/dataset ".\\data\\cns_pmpo_values.csv")]
    (tc/rename-columns ds {"Drug" :Drug
                           "CNS_pMPO" :cns-mpo
                           "CNS_pMPO_withSigmoidal"  :cns-mpo-sigm})))

(def new-params
  "read in the values of MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa. "
  (let [params (tc/dataset ".\\data\\CNS_pMPO.df.csv")]
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
  (let [new-params-small (tc/select-columns new-params [:Drug :MW :HBD :TPSA :cLogD_ACD_v15  :mbpKa])
        new (tc/drop-missing new-params-small)
        with-results  (tc/map-rows new
                                   (fn [row]
                                     ;; Call your calculate-result function on the row map
                                     {:cns-mpo-calc (calculate-result row sigmoid-correction?)}));))
        with-results (tc/select-columns with-results [:Drug :cns-mpo-calc])]
    (if sigmoid-correction?
      (tc/rename-columns with-results {:cns-mpo-calc :cns-mpo-sigm-calc})
      with-results)))

(defn round-number
  " clean up float number to a specific precision"
  [precision num]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* num factor)) factor)))

cns-mpo | :cns-mpo-calc | :cns-mpo-sigm | :cns-mpo-sigm-calc

(defn report-results
  "combine into a df the test df and the calculated values"
  []
  (let [results-file "test/test-results-table.csv" ; output file
        float-precision 2 ; number of decimal places
        output-file-path (-> (java.io.File. results-file) .getAbsolutePath)
        calculated-pmpo-no-sigmoid (calulated-pmpo-values false)
        calculated-pmpo-sigmoid (calulated-pmpo-values true)
        ds-joint-one (tc/left-join  calculated-pmpo-no-sigmoid (get-test-dataset) :Drug)
        ds-results (tc/left-join  ds-joint-one calculated-pmpo-sigmoid :Drug)
        ds-results (tc/select-columns ds-results [:Drug :cns-mpo :cns-mpo-calc :cns-mpo-sigm :cns-mpo-sigm-calc])
        ds-results (-> ds-results
                       (tc/map-columns :cns-mpo           (fn [val] (round-number float-precision val)))
                       (tc/map-columns :cns-mpo-calc      (fn [val] (round-number float-precision val))) ; Round to 2 decimal places
                       (tc/map-columns :cns-mpo-sigm      (fn [val] (round-number float-precision val)))
                       (tc/map-columns :cns-mpo-sigm-calc (fn [val] (round-number float-precision val))))]
    (tc/write!  ds-results results-file)
    (println (str "the results are saved in " output-file-path))
    (tc/print-dataset ds-results)))

(report-results) 