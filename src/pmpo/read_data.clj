(ns pmpo.read-data
  (:require
   [tablecloth.api :as tc]
   [pmpo.model :as md]))


(defn new-params
  "read in the values of ID, MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa. from the csv file 
   return dataframe"
  [filename-in]
  (let [params (tc/dataset filename-in)];".\\data\\input.csv")]
    (-> params
        (tc/rename-columns
         (into {} (map (fn [col-name] [col-name (keyword col-name)])
                       (tc/column-names params)))))))

(defn calculate-result
  "Calculates the combined pmpo a single value based on MW, HBD, TPSA, cLogD_ACD_v15, and mbpKa. apply sigmoid correction
  return sum on contributions as a float value for a row"
  [{:keys [MW HBD TPSA cLogD_ACD_v15 mbpKa]} sigmoid-correction?]
  (+ (md/calculate-contribution :mw MW sigmoid-correction?)
     (md/calculate-contribution :hbd HBD sigmoid-correction?)
     (md/calculate-contribution :psa TPSA sigmoid-correction?)
     (md/calculate-contribution :clogd cLogD_ACD_v15 sigmoid-correction?)
     (md/calculate-contribution :pka mbpKa sigmoid-correction?)))

(defn calulated-pmpo-values
  "create a dataframe with the calculated pmpo values by mapping each row of a dataframe with all the input values"
  [sigmoid-correction? filename-in]
  (let [new-params-small (tc/select-columns ( new-params filename-in) [:ID :MW :HBD :TPSA :cLogD_ACD_v15 :mbpKa])
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
  "calcualte two separate dataframes - with and without sigmoid corrections -  then 
   return a dataframe being a joint of the two dataframes"
  [filename-in]
  (let [calculated-pmpo-no-sigmoid (calulated-pmpo-values false filename-in)
        calculated-pmpo-sigmoid (calulated-pmpo-values true filename-in)]
    (tc/left-join  calculated-pmpo-no-sigmoid calculated-pmpo-sigmoid :ID)))

