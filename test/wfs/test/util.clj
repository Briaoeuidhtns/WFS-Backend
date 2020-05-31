(ns wfs.test.util
  (:require
   [clojure.walk :as walk]
   [clojure.set :refer [subset?]]))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk (fn [node]
                   (cond (map? node) (into {} node)
                         (seq? node) (vec node)
                         :else node))
                 m))

(defn submap?
  [m1 m2]
  (subset? (set m1) (set m2)))
