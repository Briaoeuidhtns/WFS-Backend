(ns wfs.server
  (:require
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [io.pedestal.http :as http]
   [wfs.auth.interceptor :as auth.interceptors]))


(defn- service-map
  [schema options]
  (let [opt          (merge options {})
        default      (lp/default-interceptors schema opt)
        interceptors (lp/inject default
                                auth.interceptors/user-info
                                :after
                                ::lp/inject-app-context)]
    (lp/service-map schema (assoc opt :interceptors interceptors))))

(defrecord Server [schema-provider server]
  component/Lifecycle
    (start [self]
      (assoc self
        :server (-> schema-provider
                    :schema
                    (lp/service-map {:graphiql true})
                    http/create-server
                    http/start)))
    (stop [self]
      (http/stop server)
      (assoc self :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {})
                            [:schema-provider])})
