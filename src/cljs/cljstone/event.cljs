(ns cljstone.event
  (:require [schema.core :as s]))

(def Event
  (s/enum
    :on-before-attack
    :on-after-attack))


; notes
; where do these channels / queues / etc live?
; is this part of the board somewhere?
; as opposed to eg being part of minions..
; hm my mental model is still really shaky
; i can imagine eg minions subscribing to :on-before-attack, :on-after-attack, etc events
; and the board is what *issues* those events, maybe? like [:on-before-attack origin-character-id destination-character-id]?
; so how can this setup cause changes to the world? eg how does blessing of wisdom work? [i guess with a swap! call that affects the relevant player's hand/deck
; how does ogre brute work? this is less clear to me - how do we cause destination-character-id to change to something else?


; so do you set up the ogre brute, and it has a channel that's listening for on-before-attack events
; and when it gets one, it does some work and then *returns* a character id?
; can event handlers - or whatever they're called in this context - *return* values? they must be able to, right?

; todo - minions should actually die via on-after-taking-damage, for knife juggler / explosive trap


; what's the return value of event handlers for :on-after-taking-damage?
; how do they signify that they've died or not? also, how does gahz'rilla work?
; i guess gahz'rilla works by just applying a modifier like {:health -1 :attack 32}
; and taking damage is the same, it's just applying a modifier, so no need to worry about that in the context of knife juggler / explosive trap
; so *where* does the clean-minions-up-on-death logic live?
; does the board listen for when modifiers are applied?
; so whenever a modifier is applied, the board is notified, and it cleans up dead minions if it finds any, and fires their :on-death? that could work......


; deathrattles are :on-death
