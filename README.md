# A Reagent Modal Dialog #

How to implement a very lightweight modal dialog in `Reagent`

Background
----------

There is already a [recipe][Reagent-Bootstrap-Modal] for `Reagent`
modal windows using `Bootstrap`.

Below is an alternative that follows the approach posted by the
[W3schools][W3SchoolsModal].

Differences from [W3schools][W3SchoolsModal]:

* No DOM node manipulation--we rely on `Reagent` atoms instead
* Styling done using [`tailwindcss`][tailwindcss]: this is not a
  requirement; please take a look at the [W3schools][W3SchoolsModal]
  code and use whatever approach you'd like for CSS

[W3SchoolsModal]: https://www.w3schools.com/howto/tryit.asp?filename=tryhow_css_modal "W3schools Modal"
[tailwindcss]: https://tailwindcss.com/ "tailwindcss"
[Reagent-Bootstrap-Modal]: https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/bootstrap-modal "Bootstrap modal window"

# Some Comments on the Code #

This is a minimal example that serves as amodel for login or more
complex forms. Though I hate modal dialogs from a user perspective, we
need to have a way to implement them.

## Architecture ##

We implement a very simple variant of Facebook's *Flux
Architecture*. We hold state in a `Reagent` atom and we use
`core.async` to implement an `event-queue`. Events are placed in the
queue as a result of the actions of the user. Events are just data--a
vector with a keyword: `[:show-modal]` or `[:hide-modal]`.

Event dispatch takes place by having code listening for events on the
`event-queue`: we change the value of the CSS `display` property to
`none` if we want to hide the modal or `block` if we want to display
it directly as a result of an event being dispatched:

```clojure
(def modal-display (r/atom {:background-color "rgba(0,0,0,0.4)"
                            :display "none"}))

(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :show-modal (swap! modal-display #(assoc % :display "block"))
    :hide-modal (swap! modal-display #(assoc % :display "none")))
  (recur (<! event-queue)))
```

Note that state is mutated only in the lines above. Regretably, a
`stop-propagation` side-effect has to be implemented directly on the
`:on-click` function associated with the modal content element because
we need to stop the propagation of the `click` event right then and
there (it doesn't work to do so asynchronously).

## Reagent Elements ##

The intention is to keep the code for the main component as clear and
simple as possible:

```clojure
(defn main-component []
  [:div 
   [title-component]
   [show-modal-button]
   [modal-container modal-dialog]
])
```

The `title-component` is a simple heading:

```clojure
(defn title-component []
  [:h1.p-2.text-3xl "Modal Example"])
```

Here `p-2` and `text-3xl` are `tailwindcss` utility classes. I find it
useful to include CSS at a higher level in the component
themselves. Time will tell if this is a good idea.

The `show-modal-button` is also simple--a button with an `:on-click`
function that launches the modal. When the user clicks on this button

* We put a `[:show-modal]` event on the event queue which will result
  in the modal dialog being shown to the user
* We blur the button so that it is no longer focused. We could move
  this side-effect to the event dispatch handler but it doesn't make
  that much difference and we are forced to handle some side-effects
  in the view code anyway, as discussed above.

```clojure
(defn show-modal-button []
  [:button.m-2.p-2.border.border-solid.border-black.rounded-md.bg-gray-200
   {:on-click #(do (put! event-queue [:show-modal])
                   (.activeElement.blur js/document))}
   "Open Modal"])
```

## The Modal ##

We implement the modal dialog through 2 components: a
`modal-container` and a `modal-dialog`. The container is used to hold
the modal dialog in an element that fills the entire window (absolute
position at (0, 0), 100% width and height) with background colour that
dims the content below (*z index* of 10) and highlights the contents
of the `modal-dialog`:

```clojure
(defn modal-container [dialog]
  ;; modal background container
  [:div
   {:class "fixed z-10 top-0 left-0 w-full h-full overflow-auto" 
    :style @modal-display
    ;; clicking outside of modal closes the modal
    :on-click #(do (put! event-queue [:hide-modal])
                   (.stopPropagation %))}
   [dialog]])
```

Note the `:style` attribute: it holds a `Reagent` atom; any changes in
the value of this atom will cause the component to re-render. Note
also that clicking anywhere in the window *except* inside the modal
dialog itself will result in the modal dialog being closed (we
dispatch a `[:hide-modal]` event in the `:on-click` function of the
modal container.

The actual content displayed is wrapped in a `div` with an `:on-click`
event that stops the propagation of the event so that the modal cannot
be dismissed from *inside* the modal dialog:

```clojure
(defn modal-dialog
  "A modal dialog with text and a button to close it"
  []
  [:div ;; container to hold contents
   {:class "mx-auto p-4 modal-content flex items-center w-5/6 mt-32
            bg-gray-500 border border-solid border-black"
    :on-click #(.stopPropagation %)} ; disable closing *inside* modal
   [:p
    {:class "w-11/12"} "Some Text in the Modal..."]
   ;; button to close modal
   [:button
    {:class "px-3 py-2 ml-auto border border-solid border-black
             rounded-md bg-gray-400 hover:bg-gray-300 focus:bg-gray-300"
     :on-click #(put! event-queue [:hide-modal])} 
    "Close"]])
```

Note: you don't have to implement a queue to change the value of
`modal-atom`---you could instead change the value of the atom directly
in the function associated with the `:on-click` event. I like the
`core.async` approach because it keeps all the code mutating state in
one place.

# How to Run the Code #

These directions assume that you have `npm` and the proper
ClojureScript setup (Java, etc.)

1. Install with `npm`: `npm install`
2. Build CSS files: `npm run tw`
3. Open `main.cljs` in Emacs
4. `cider-jack-in-cljs`:
   * Select `shadow` for `cljs` REPL
   * Select `:app` as the build
   * Type `y` to start a browser session
   
Notice that the app might start *before* the code is compiled by
`cider` if you don't wait long enough before you start the browser
session, so you might get a message in the browser that the client is
stale. All you need to do is refresh the browser to get rid of the
error message

# Credit #

This repository leverages the work of Michael Zamansky and differs
from his `Reagent` template thusly:

* Updated to `reagent 0.10.0` and `core.async 1.0.567`
* Corresponding upgrades to `react` and `react-dom`
* `package.json` includes `onchange`
* Minor upgrade to `tailwindcss` to get rid of a low-level
  vulnerability

