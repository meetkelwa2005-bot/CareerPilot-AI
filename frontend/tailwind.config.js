/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f4f6fe',
          100: '#eaedfcf',
          200: '#d9dffa',
          300: '#bdc8f7',
          400: '#9aa9f3',
          500: '#6373e9',
          600: '#4d5cd9',
          700: '#3e4bc0',
          800: '#35409e',
          900: '#303980',
          950: '#1d214e',
        },
        slate: {
          850: '#1e293b',
          950: '#0b1329',
        }
      },
      fontFamily: {
        sans: ['Inter', 'Outfit', 'sans-serif'],
      },
      boxShadow: {
        glass: '0 8px 32px 0 rgba(31, 38, 135, 0.37)',
        glassLight: '0 8px 32px 0 rgba(255, 255, 255, 0.08)',
      }
    },
  },
  plugins: [],
}
