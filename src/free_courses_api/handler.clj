(ns free-courses-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn get-api-url [params]
  (let [url "https://sheets.googleapis.com/v4/spreadsheets/"]
    (str url (env :sheets-id) "/values/" params "?key=" (env :api-key))))

(defn normalize-sheets-data [sheets-data]
  (let [keys (first sheets-data)
        data (drop 1 sheets-data)]

    (map #(reduce conj {} %)
         (map
          (fn [values]
            (map-indexed (fn [index value] {(nth keys index) value}) values)) data))))

(defn get-sheets-value [params]
  (normalize-sheets-data ((json/read-str ((client/get (get-api-url params)) :body) :key-fn keyword) :values)))

(defroutes app-routes
  (GET "/" [] "Hello World!")
  (GET "/categories" [] (slurp "data/categories.json"))
  (GET "/courses" [] (slurp "data/courses.json"))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      wrap-json-response
      wrap-json-body))
