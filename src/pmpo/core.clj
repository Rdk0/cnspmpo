(ns pmpo.core
  (:require
   [tablecloth.api :as tc]
   [pmpo.read-data :as rd]
   [cli-matic.core :refer [run-cmd]]
   [portal.api :as p])
  (:gen-class))

(defn round-number
  " clean up float number to a specific precision"
  [precision num]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* num factor)) factor)))


(defn app [params] ;input-file output-file]
  (let [results-file (:filename-out params) ;output-file; "data/results-table.csv" ; output file
        input-file   (:filenme-in params) ;input-file ;"data/input.csv" ; input file
        float-precision 2 ; number of decimal places in the output
        input-file-path (-> (java.io.File. input-file) .getAbsolutePath)
        output-file-path (-> (java.io.File. results-file) .getAbsolutePath)
        result-table (tc/select-columns (rd/report-results input-file)  [:ID :cns-mpo-calc :cns-mpo-sigm-calc])
        result-table (-> result-table
                         (tc/map-columns :cns-mpo-calc      (fn [val] (round-number float-precision val))) ; Round to 2 decimal places
                         (tc/map-columns :cns-mpo-sigm-calc (fn [val] (round-number float-precision val))))]
    (println "All the input values (HBD,MW,TPSA,cLogD,mbpKa) are read from this file")
    (println  input-file-path)
    (tc/print-dataset result-table)
    (tc/write! result-table results-file)
    (println (str "the results are saved in " output-file-path))))


(def CONFIGURATION
  {:app  {:command "pmpo"
          :description "calculations of pMPO.  An input csv file is required with the following columns
                      MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa"
          :version "0.0.1"
          :runs app 
          :opts [{:option "filename-in"
                  :short "in"
                  :as "input file"
                  :default "./data/input.csv"
                  :type :string}
                 {:option "filename-out"
                  :short "out"
                  :as "./data/output file"
                  :default "results-table.csv"   
                  :type :string}]}})


(defn -main
  [& args]
  (println "recieved args: " args)
  (run-cmd args CONFIGURATION))
;)


(comment
  ;clj -M -m pmpo.core --filename-in my-data.csv --filename-out results.csv
  (pmpo.core/-main "--filename-in" "./data/input.csv" "--filename-out" "./data/results.csv")
(def portal (p/open))
(add-tap #'p/submit)
  (tap> :hello)
  (tap> CONFIGURATION)
  (-main )
)
  