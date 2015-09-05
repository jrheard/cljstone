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

; what's the return value of event handlers for :on-after-taking-damage?
; how do they signify that they've died or not? also, how does gahz'rilla work?
; i guess gahz'rilla works by just applying a modifier like {:health -1 :attack 32}
; and taking damage is the same, it's just applying a modifier, so no need to worry about that in the context of knife juggler / explosive trap,
; so death handling does *not* live in on-after-taking-damage
; so *where* does the clean-minions-up-on-death logic live?
; does the board listen for when modifiers are applied?
; so whenever a modifier is applied, the board is notified, and it cleans up dead minions if it finds any, and fires their :on-death? that could work......


; deathrattles are :on-death


; modifiers will be able to contain event handlers - knife juggler comes bundled with an :on-owner-summons-minion event handler,
; blessing of wisdom is an event handler that can be *applied* to a currently-living minion, and the only way it can be applied is as a Modifier


; hm - so minions will have modifiers, and those modifiers will have things like {:on-before-attack a-channel}, and we start a go-block at minion creation time
; that waits for messages on a-channel, and those messages look like destination-minion-id board-atom, and it returns / puts back values like a-new-board?
; or perhaps just swap!s the board itself without returning a new one

; worth googling: in core.async systems, where do your side effects live? in a bunch of tiny go-blocks?
; or should the go-blocks just return values and *not* perform side effects?

; ok it looks like the way to have return values is to have input channels and output channels
; so you could... have knife juggler have an :on-owner-summons-minion [input-chan output-chan] pair, perhaps? that could work. but in that case who's reading from output-chan?
; if someone's gotta read from output-chan and do something with its value, then does core.async really buy us anything over just calling the function in the first place?
; unlesss... what if knife juggler doesn't *return* an output channel, but instead *takes* one?
; what if we have a go-block that constantly tries to pull from a board-updates channel, and whenever it gets a message it calls swap! on the baord with it?
; might be dumb, might work. todo: figure out how that plays with the combat log / (cause-damage) function idea.
; gonna have to standardize on a format/schema for messages to that output channel.
; or perhaps could return functions + args, like [draw-card :half-1], or [cause-damage the-knife-juggler-minion another-character], etc. that could work.

; TODO - look into async/merge

(js/console.log "EVENT")
