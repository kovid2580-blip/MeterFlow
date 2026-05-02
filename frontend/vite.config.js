import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes("node_modules")) {
            return undefined;
          }

          if (/[\\/]node_modules[\\/](react|react-dom|scheduler|prop-types)[\\/]/.test(id)) {
            return "react";
          }

          if (id.includes("lucide-react")) {
            return "icons";
          }

          if (id.includes("recharts")) {
            return "recharts";
          }

          if (
            /[\\/]node_modules[\\/](d3-|recharts-scale|victory-vendor|decimal\.js-light|lodash|internmap|eventemitter3|fast-equals|clsx|tiny-invariant)/.test(id)
          ) {
            return "charts-vendor";
          }

          return "vendor";
        }
      }
    }
  }
});
