import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeSanitize from "rehype-sanitize";

type MarkdownTextProps = {
    content: string;
    className?: string;
};

export function MarkdownText({ content, className }: MarkdownTextProps) {
    return (
        <div className={className}>
            <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeSanitize]}
                components={{
                    a: ({ _, ...props }) => (
                        <a
                            {...props}
                            className="text-blue-600 underline decoration-blue-400 underline-offset-2"
                            target="_blank"
                            rel="noreferrer"
                        />
                    )
                }}
            >
                {content}
            </ReactMarkdown>
        </div>
    );
}
