import { color } from "framer-motion";

export const SectionWrapper = ({ heading, name, flex }) => {
    const Tag = name || "span";
    const flexVal = flex || 10;
    return (
        <main style={{ display: "flex", justifyContent: "center", marginTop: "2rem", marginBottom: "2rem" }}>
            <div style={{ display: "flex", alignItems: "center", width: "100%", marginLeft: "0.75rem" }}>
                <hr style={{ flex: flexVal, border: "none", borderTop: "1px solid #e0e0e0" }} />
                <div style={{ margin: "0 2rem", whiteSpace: "nowrap" }}>
                    <Tag style={{color: "#333"}}>{heading}</Tag>
                </div>
                <hr style={{ flex: flexVal, border: "none", borderTop: "1px solid #e0e0e0" }} />
            </div>
        </main>
    );
};
