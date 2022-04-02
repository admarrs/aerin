module.exports = {
  purge: {
    enabled: false,
    content: [
      './src/**/*.clj',
      './src/**/*.cljs',
    ],
  },
  darkMode:   'media', // or 'media' or 'class'
  theme: {
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [],
}