(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'pmpo/core) ; Replace 'your-group-id' with your own
(def version "0.0.1");(format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(def basis ( b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"})
  (println "Cleaned target directory."))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources" "data"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :ns-compile '[pmpo.core]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'pmpo.core})
  (println (format "Jar file created: \"%s\"" uber-file))) ; This is the crucial line to add or fix

