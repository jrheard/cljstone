(ns cljstone.html
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s])
  (:require-macros [enfocus.macros :as em])
  (:use [cljstone.minion :only [Minion get-attack get-health]]
        [cljstone.hero :only [Hero]]
        [cljstone.board :only [BoardHalf perform-attack]]))

(def root-template "resources/public/index.html")

(em/defsnippet hero-snippet :compiled "resources/public/index.html" ".hero div"
  [hero]
  ".name" (ef/content (:name hero)))

(em/defsnippet minion-snippet :compiled "resources/public/index.html" ".minion"
  [minion]
  ".minion" (ef/set-attr :data-minion-id (:id minion))
  ".name" (ef/content (:name minion))
  ".attack" (ef/content (str (get-attack minion)))
  ".health" (ef/content (str (get-health minion))))

(em/defsnippet board-half-snippet :compiled "resources/public/index.html" ".board-half"
  [board-half characters-by-id]
  ".hero" (ef/content (hero-snippet (:hero board-half)))
  ".minion-container" (ef/content
                        (map #(minion-snippet (characters-by-id %))
                             (:minion-ids board-half))))

(em/deftemplate board-template :compiled "resources/public/index.html" [board]
  ".board" (ef/content [(board-half-snippet (:half-1 board) (:characters-by-id board))
                        (board-half-snippet (:half-2 board) (:characters-by-id board))]))

(defn get-data-transfer [goog-event]
  (-> goog-event
      .getBrowserEvent
      .-dataTransfer))

(defn get-target-dataset [goog-event]
  (-> goog-event
      .-currentTarget
      .-dataset))

(defn draw-board [board]
  (ef/at "body" (ef/content (board-template @board)))

  (ef/at ".minion" (ev/listen :dragstart
                              (fn [e]
                                (js/console.log "dragstart")
                                (let [minion-id (.-minionId (get-target-dataset e))
                                      data-transfer (get-data-transfer e)]
                                  (.setData data-transfer "text/plain" minion-id)))))

  (ef/at ".minion"
    (ev/listen :dragover
               (fn [e]
                 (.preventDefault e))))

  (ef/at ".minion" (ev/listen :drop
                              (fn [e]
                                (let [origin-minion-id (.getData (get-data-transfer e) "text/plain")
                                      destination-minion-id (.-minionId (get-target-dataset e))]
                                  (js/console.log origin-minion-id)
                                  (js/console.log destination-minion-id)
                                  (swap! board perform-attack origin-minion-id destination-minion-id)
                                  (draw-board board)
                                  (.preventDefault e))))))



