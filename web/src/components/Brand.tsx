import { Blocks } from "lucide-react";

export function Brand({ light = false }: { light?: boolean }) {
  return (
    <span className={`brand ${light ? "brand--light" : ""}`}>
      <span className="brand__mark">
        <Blocks size={18} strokeWidth={2.2} />
      </span>
      <span>OrderFlow</span>
    </span>
  );
}
