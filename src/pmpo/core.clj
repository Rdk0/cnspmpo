(ns pmpo.core
  (:require
   [tablecloth.api :as tc]
   [pmpo.read-data :as rd]
   [cli-matic.core :refer [run-cmd]])
  (:gen-class))


(defn round-number
  " clean up float number to a specific precision"
  [precision num]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* num factor)) factor)))


(defn app [{:keys [i o]}] ;input-file output-file]
  (let [results-file o  ;output-file; "data/results-table.csv" ; output file
        input-file   i ;input-file ;"data/input.csv" ; input file
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
   {:command "pmpo"
          :description "calculations of cns-mpo values based on 
                      MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa"
          :version "0.0.1"
          :runs app 
          :opts [{:option "i"
                  :as "input file"
                  :default "./data/input.csv"
                  :type :string}
                 {:option "o"
                  :as "output file"
                  :default "./data/results-table.csv"   
                  :type :string}]})


(defn -main
  [& args]
  (println "recieved args: " args)
  (run-cmd args CONFIGURATION))


  