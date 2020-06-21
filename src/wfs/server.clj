(ns wfs.server
  (:require
   [com.stuartsierra.component :as component]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [com.walmartlabs.lacinia.pedestal2 :as lp2]
   [com.walmartlabs.lacinia.pedestal.subscriptions :as lp.sub]
   [io.pedestal.http :as http]
   [io.pedestal.interceptor.chain :as chain]
   [wfs.auth.interceptor :as auth.interceptors]
   [taoensso.timbre :as log]))

(defrecord Server [schema-provider server port]
  component/Lifecycle
    (start [self]
      (let [api-path "/api"
            ide-path "/ide"
            asset-path "/assets/graphiql"
            app-context nil
            port 8888
            compiled-schema (:schema schema-provider)
            interceptors
            (-> (lp2/default-interceptors compiled-schema app-context)
                (lp/inject auth.interceptors/user-info
                           :after
                           ::lp2/inject-app-context)
                (lp/inject {:name ::spy
                            :error (fn [context error]
                                     (log/error error)
                                     (assoc context ::chain/error error))}
                           :before
                           ::auth.interceptors/user-info))

            subscription-interceptors
            (-> (lp.sub/default-subscription-interceptors compiled-schema
                                                          app-context)
                #_(lp/inject auth.interceptors/user-info
                             :after
                             ::lp.sub/inject-app-context))

            options {:api-path api-path
                     :ide-path ide-path
                     :asset-path asset-path
                     :app-context app-context
                     :port port
                     :subscription-interceptors subscription-interceptors}
            routes
            (into #{[api-path :post interceptors :route-name ::graphql-api]
                    [ide-path
                     :get
                     (lp2/graphiql-ide-handler options)
                     :route-name
                     ::graphiql-ide]}
                  (lp2/graphiql-asset-routes asset-path))]
        (assoc self
          :server (-> {:env :dev
                       ::http/routes routes
                       ::http/port port
                       ::http/type :jetty
                       ::http/join? false}
                      lp2/enable-graphiql
                      (lp2/enable-subscriptions compiled-schema options)
                      http/create-server
                      http/start))))
    (stop [self] (http/stop server) (assoc self :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {:port 8888}) [:schema-provider])})
