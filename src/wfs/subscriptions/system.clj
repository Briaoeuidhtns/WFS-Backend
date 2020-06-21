(ns wfs.subscriptions.system
  (:require
   [clojure.core.async :as a]
   [com.stuartsierra.component :as component]))

(defrecord PublicationManager [chan pub]
  component/Lifecycle
    (start [self]
      (let [c (a/chan)
            p (a/pub c
                     #(select-keys %
                                   [:deliver-to
                                    :type]))]
        (assoc self
          :chan c
          :pub p)))
    (stop [self]
      (assoc
        self
        :chan nil
        :pub nil)))

(defn new-sub
  []
  {:sub (map->PublicationManager {})})
