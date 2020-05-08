(ns wfs.session
  (:require [com.stuartsierra.component :as component]
            [expiring-map.core :as em]))

(defrecord SessionManager [ttl sessions]
  component/Lifecycle

  (start [self]
    (assoc self :sessions (em/expiring-map
                           (:ttl self)
                           {:expiration-policy :access})))

  (stop [self]
    (assoc self :sessions nil)))

(defn new-session-manager
  [session-ttl]
  {:session-manager (->SessionManager session-ttl nil)})
