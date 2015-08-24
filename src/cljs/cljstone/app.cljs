(ns cljstone.app
  (:require [cljs-uuid-utils.core :as uuid]
            [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s]
            [cljstone.board :as board]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html]))

(html/draw-board (board/make-board hero/jaina hero/thrall))

(js/console.log (uuid/make-random-uuid))
(js/console.log (uuid/uuid-string (uuid/make-random-uuid)))

(ef/at ".minion" (ev/listen :dragstart
                            (fn [e]
                              (js/console.log e)
                              (.setData (.-dataTransfer (.-event_ e))
                                        "source-thing"
                                        (.-target e)))))

(ef/at ".minion" (ev/listen :drop
                            (fn [e]
                              (.preventDefault e)
                              (.stopPropagation e)
                              (js/console.log (.getData (.-dataTransfer e) "source-thng")))))
