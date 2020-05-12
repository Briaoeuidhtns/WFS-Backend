(ns wfs.schema-test
  (:require [wfs.schema :as sut]
            [clojure.test :as t]))

(t/deftest schema-test
  (t/is (sut/load-schema (sut/new-schema-provider))))
