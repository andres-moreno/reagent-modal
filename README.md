# A Reagent Modal Dialog #

How to implement a very lightweight modal dialog in `Reagent`

Background
----------

There is already a [recipe][Reagent-Bootstrap-Modal] for `Reagent`
modal windows using `Bootstrap`.

Below is an alternative that follows the approach posted by the
[W3schools][W3SchoolsModal] using only Javascript (no Boostrap, no
JQuery). We follow this approach with suitable changes where needed.

Differences from [W3schools][W3SchoolsModal]:

* No DOM node manipulation--we rely on `Reagent` atoms instead
* Styling done using [`tailwindcss`][tailwindcss]: this is not a
  requirement; please take a look at the [W3schools][W3SchoolsModal]
  code and use whatever approach you'd like for CSS

[W3SchoolsModal]: https://www.w3schools.com/howto/tryit.asp?filename=tryhow_css_modal "W3schools Modal"
[tailwindcss]: https://tailwindcss.com/ "tailwindcss"
[Reagent-Bootstrap-Modal]: https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/bootstrap-modal "Bootstrap modal window"

# On the Code #

This is a minimal example that serves as a model for a basic modal
dialog or even a form (e.g. login). I don't much like modal dialogs
but they are a staple of front-end applications.

## Architecture ##

We implement a very simple variant of Facebook's *Flux
Architecture*. We hold state in a `Reagent` atom and we use
`core.async` to implement an `event-queue`. Events are placed in the
queue as a result of the actions of the user. Events are just data--a
vector with a keyword--and there are just two of them: `[:show-modal]`
or `[:hide-modal]`.

Event dispatch takes place by having code listening for events on the
`event-queue`: we change the value of the CSS `display` property of
the modal element to `none` if we want to hide the modal, and change
it to `block` if we want to display it.

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

Note that state is mutated only in the lines above. Unfortunately, we
were not able to isolate side-effects in this section of the code: we
need to stop the propagation of click events *in situ* so that the
code will behave as expected. More details below.

The sharp-eyed reader will have noticed that the atom also holds the
background color for the modal--black with some opacity. I haven't
figured out how to get the same effect using `tailwindcss` utility
classes. We could have implemented a custom CSS class and included the
background color and other styling elments for the modal
there. Alternatively, one can extend the theme as suggested in this
[feature request][TailwindCSS-feat-req]. This is an artifact of using
`tailwindcss` and not being sure on what's the best way to
implement. Besides, it shows that we can hold other properties in the
atom as well as use CSS classes at the same time.

NOTE: this event-based architecture is not needed. One could just as
well modify the value of the `modal-display` atom in the `:on-click`
handlers themselves.

[TailwindCSS-feat-req]: https://github.com/tailwindcss/tailwindcss/issues/1258 "Opacity feature request"

## Reagent Elements ##

We want to keep the code for the main component as clear and simple as
possible:

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
useful to include higher level CSS in the elements themselves which is
the pitch `tailwindcss` makes. Time will tell if this is a good idea.

The `show-modal-button` is also simple: a button with an `:on-click`
function that launches the modal. When the user clicks on this button

* We put a `[:show-modal]` event on the event queue which will result
  in the modal dialog being shown to the user
* We blur the `show-modal` button so that it is no longer on focus. We
  could move this side-effect to the event dispatch handler but it
  doesn't make that much difference and we are forced to handle some
  side-effects in the view code anyway.

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
the modal dialog in an element that fills the *entire* window
(absolute position at (0, 0), 100% width and height) with background
colour that dims the content below (*z index* of 10) and highlights
the contents of the `modal-dialog`:

```clojure
(defn modal-container [dialog]
  ;; modal background container
  [:div
   {:class "fixed z-10 top-0 left-0 w-full h-full overflow-auto" 
    :style @modal-display
    ;; clicking outside of modal closes the modal
    :on-click #(do (put! event-queue [:hide-modal]))}
   [dialog]])
```

The `:style` attribute derefences a `Reagent` atom; any changes in the
value of this atom will cause the component to re-render. We also put
a `[:hide-modal]` event in the queue if the user clicks on the
`modal-container`, so the modal will be close whenever the user clicks
anywhere in the screen *except* for the modal dialog itself, where we
intercept the `:on-click` event and stop its propagation:

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

This `Reagent` component has two HTML elements: a `<p>` with some
text, and a `<button>` to close the modal--these elements can be
substituted for any other code as can any of the styling.

# How to Run the Code #

These directions assume that you have `npm` installed and the proper
ClojureScript setup (Java, etc.) to run `Reagent` applications.

1. Install dependencies with `npm`: `npm install`
2. Build CSS files: `npm run tw`
3. Open `main.cljs` in Emacs
4. `cider-jack-in-cljs`:
   * Select `shadow` for `cljs` REPL
   * Select `:app` as the build
   * Type `y` to start a browser session
   
Notice that the app might start *before* the code is compiled by
`cider` if you don't wait long enough before you start the browser
session. In this case you might get a message in the browser that the
client is stale. All you need to do is refresh the browser to get rid
of the error message

# Credit #

This repository leverages the work of Michael Zamansky, both his
videos and template.
