(ns wfs.core-test
  (:require  [clojure.test :refer :all]
             [wfs.core :refer :all]))

(deftest trivial-test
  (is true))

(deftest main-test
  (binding [*out* (java.io.StringWriter.)]
    (-main)
    (is (= "Hello World!\n" (.toString *out*)))))
