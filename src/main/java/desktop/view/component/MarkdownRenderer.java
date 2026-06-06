package desktop.view.component;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Markdown → HTML 渲染器。
 */
public class MarkdownRenderer {

    private static final String HTML_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8"/>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: 'Open Sans', 'Microsoft YaHei', sans-serif;
                    font-size: 14px;
                    line-height: 1.6;
                    color: #B0B8C4;
                    background: transparent;
                    padding: 0;
                }
                p { margin: 0 0 8px 0; }
                strong { color: #E8ECF1; font-weight: 600; }
                em { color: #6B7685; }
                a { color: #22D3EE; text-decoration: none; }
                a:hover { text-decoration: underline; }
                code {
                    font-family: 'JetBrains Mono', 'Consolas', monospace;
                    background: #0D1017;
                    color: #E6EDF3;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-size: 13px;
                }
                pre {
                    background: #0D1017;
                    border-radius: 8px;
                    padding: 12px 16px;
                    margin: 8px 0;
                    overflow-x: auto;
                }
                pre code {
                    background: transparent;
                    padding: 0;
                    font-size: 13px;
                    line-height: 1.4;
                }
                blockquote {
                    border-left: 3px solid #0E7490;
                    margin: 8px 0;
                    padding: 4px 12px;
                    color: #6B7685;
                    background: rgba(14, 116, 144, 0.06);
                }
                ul, ol { margin: 4px 0 8px 20px; }
                li { margin: 2px 0; }
                h1, h2, h3 { color: #E8ECF1; margin: 12px 0 6px 0; }
                h1 { font-size: 18px; } h2 { font-size: 16px; } h3 { font-size: 15px; }
                table { border-collapse: collapse; margin: 8px 0; width: 100%; }
                th, td {
                    border: 1px solid #2E3440;
                    padding: 6px 12px;
                    text-align: left;
                }
                th { background: #1E222A; color: #E8ECF1; font-weight: 600; }
                hr { border: none; border-top: 1px solid #2E3440; margin: 12px 0; }
            </style>
        </head>
        <body>%s</body>
        </html>
        """;

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderer() {
        List<Extension> extensions = List.of(TablesExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
        this.renderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    public String renderToHtml(String markdown) {
        Node document = parser.parse(markdown != null ? markdown : "");
        String bodyHtml = renderer.render(document);
        return String.format(HTML_TEMPLATE, bodyHtml);
    }

    public String renderToFragment(String markdown) {
        Node document = parser.parse(markdown != null ? markdown : "");
        return renderer.render(document);
    }
}
