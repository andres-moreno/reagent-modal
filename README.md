# shadow-cljs-template #

A minimal template for `reagent` using `shadow-cljs-template` based on
Michael Zamansky's work.

# How to Use it #

1. Install `npm` messages: `npm install`
2. Build CSS files: `npm run tw`
3. Compile the app: `npx shadow-cljs compile app`
4. Open `main.cljs` in Emacs
5. `cider-jack-in-cljs`:
   * Select `shadow` for `cljs` REPL
   * Select `:app` as the build
   * Type `y` to start a browser session
   
Notice that the app might start *before* the code is compiled by
`cider` so you might get a message in the browser that the client is
stale. All you need to do is refresh the browser to get rid of the
error message

# Differences with Michael Zamansky's Template #

* Updated to `reagent 0.10.0` and `core.async 1.0.567`
* Corresponding upgrades to `react` and `react-dom`
* `package.json` includes `onchange`
* Minor upgrade to `tailwindcss` to get rid of a low-level
  vulnerability

