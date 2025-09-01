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


(defn app  [opts] ;input-file output-file]
  (let [result-file (:o opts)  
        input-file   (:i opts) 
        float-precision 2 ; number of decimal places in the output
        result-table (tc/select-columns (rd/report-results input-file)  [:ID :cns-mpo-calc :cns-mpo-sigm-calc])
        result-table (-> result-table
                         (tc/map-columns :cns-mpo-calc      (fn [val] (round-number float-precision val))) ; Round to 2 decimal places
                         (tc/map-columns :cns-mpo-sigm-calc (fn [val] (round-number float-precision val))))]
    (println "All the input values (HBD,MW,TPSA,cLogD,mbpKa) are read from this file")
    (println  (str "the input file is: " (-> (java.io.File. input-file) .getAbsolutePath)))
    (tc/print-dataset result-table)
    (tc/write! result-table result-file)
    (println (str "the results are saved in: " (-> (java.io.File. result-file) .getAbsolutePath)))))
 ;(println result-file input-file)))

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


  (comment 
    ;clj -M -m pmpo.core --i "./data/input.csv" --o "./data/out2.csv"
    ;clj -T:build uber
    ;java -jar target/core-0.0.1-standalone.jar --help
    ;java -jar target/core-0.0.1-standalone.jar --i ./data/input.csv --o ./data/out.csv
  )