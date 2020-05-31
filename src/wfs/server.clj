(ns wfs.server
  (:require
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [io.pedestal.http :as http]
   [io.pedestal.interceptor.chain :as chain]
   [wfs.auth.interceptor :as auth.interceptors]
   [taoensso.timbre :as log]))

(defn- service-map
  [schema options]
  (let [opt          (merge options {})
        interceptors
        (-> (lp/default-interceptors schema opt)
            (lp/inject auth.interceptors/user-info
                       :after
                       ::lp/inject-app-context)
            (lp/inject {:name ::spy
                        :error (fn [context error]
                                 (log/error error)
                                 (assoc context ::chain/error error))}
                       :before
                       ::auth.interceptors/user-info))]
    (lp/service-map schema (assoc opt :interceptors interceptors))))

(defrecord Server [schema-provider server port]
  component/Lifecycle
    (start [self]
      (assoc self
        :server (-> schema-provider
                    :schema
                    (service-map {:graphiql true :port port})
                    http/create-server
                    http/start)))
    (stop [self]
      (http/stop server)
      (assoc self :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {:port 8888})
                            [:schema-provider])})
