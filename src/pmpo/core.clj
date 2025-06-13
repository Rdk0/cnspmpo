(ns pmpo.core
  (:require
   [tablecloth.api :as tc]
   [pmpo.read-data :as rd])
  (:gen-class))

(defn round-number
  " clean up float number to a specific precision"
  [precision num]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* num factor)) factor)))


(defn -main []
  (let [results-file "data/results-table.csv" ; output file
        input-file   "data/input.csv" ; input file
        float-precision 2 ; number of decimal places in the output
        input-file-path (-> (java.io.File. input-file) .getAbsolutePath)
        output-file-path (-> (java.io.File. results-file) .getAbsolutePath)
        result-table (tc/select-columns (rd/report-results)  [:ID :cns-mpo-calc :cns-mpo-sigm-calc])
        result-table (-> result-table
                         (tc/map-columns :cns-mpo-calc      (fn [val] (round-number float-precision val))) ; Round to 2 decimal places
                         (tc/map-columns :cns-mpo-sigm-calc (fn [val] (round-number float-precision val))))]
    (println "All the input values (HBD,MW,TPSA,cLogD,mbpKa) are read from this file")
    (println  input-file-path)
    (tc/print-dataset result-table)
    (tc/write! result-table results-file)
    (println (str "the results are saved in " output-file-path))))

