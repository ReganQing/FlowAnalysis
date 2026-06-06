package desktop.view.component;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;

/**
 * 轻量级 Markdown 渲染组件 — 使用 JavaFX TextFlow 渲染。
 * <p>
 * 用于流式响应场景，无需 WebView / jfxwebkit 原生库。
 * 支持加粗、斜体、行内代码、代码块、标题、列表等常见格式。
 * <p>
 * 注意：不支持表格渲染（TextFlow 无法呈现 HTML 表格结构），
 * 完整渲染请使用 WebView + MarkdownRenderer。
 */
public class MarkdownTextFlow extends VBox {

    /** 不启用 TablesExtension — TextFlow 无法渲染表格，避免静默降级为纯文本 */
    private static final Parser PARSER = Parser.builder().build();

    private static final String COLOR_TEXT = "#CBD5E1";
    private static final String COLOR_HEADING = "#F1F5F9";
    private static final String COLOR_CODE_BG = "#0D1117";
    private static final String COLOR_CODE_TEXT = "#E6EDF3";
    private static final String COLOR_LINK = "#3B82F6";
    private static final String COLOR_QUOTE = "#94A3B8";
    private static final String COLOR_QUOTE_BORDER = "#C8A97E";
    private static final String FONT_MONO = "'JetBrains Mono', 'Consolas', monospace";
    private static final String FONT_SANS = "'Open Sans', 'Microsoft YaHei', sans-serif";

    /** 流式更新节流间隔（毫秒），避免每个 token 都重新解析渲染 */
    private static final long THROTTLE_INTERVAL_MS = 80;

    private static final double SPACING = 4;

    private String pendingMarkdown;
    private long lastRenderTime = 0;

    public MarkdownTextFlow() {
        setSpacing(SPACING);
    }

    /**
     * 解析并渲染 Markdown 文本。可重复调用（流式更新）。
     * 内置节流机制：距离上次渲染不足 {@link #THROTTLE_INTERVAL_MS} 毫秒时，
     * 仅缓存内容不重新渲染，等待下次调用或 {@link #forceRender()}。
     */
    public void updateMarkdown(String markdown) {
        pendingMarkdown = markdown;
        long now = System.currentTimeMillis();
        if (now - lastRenderTime >= THROTTLE_INTERVAL_MS) {
            renderNow(markdown);
        }
    }

    /**
     * 强制立即渲染当前缓存的内容（流式结束时调用）。
     */
    public void forceRender() {
        if (pendingMarkdown != null) {
            renderNow(pendingMarkdown);
        }
    }

    private void renderNow(String markdown) {
        getChildren().clear();
        lastRenderTime = System.currentTimeMillis();

        if (markdown == null || markdown.isEmpty()) {
            return;
        }

        Node document = PARSER.parse(markdown);
        RenderVisitor visitor = new RenderVisitor();
        visitor.render(document);
    }

    // ── 渲染访问器 ────────────────────────────────────────────

    private class RenderVisitor extends AbstractVisitor {

        private TextFlow currentBlock;

        void render(Node node) {
            node.accept(this);
            flushBlock();
        }

        // ── 块级元素 ──

        @Override
        public void visit(Paragraph paragraph) {
            flushBlock();
            currentBlock = createParagraphFlow();
            visitChildren(paragraph);
        }

        @Override
        public void visit(Heading heading) {
            flushBlock();

            TextFlow flow = new TextFlow();
            double fontSize = switch (heading.getLevel()) {
                case 1 -> 18;
                case 2 -> 16;
                default -> 15;
            };
            addInlineChildren(heading, flow,
                    "-fx-font-weight: bold; -fx-fill: " + COLOR_HEADING
                            + "; -fx-font-size: " + fontSize + "px;");
            getChildren().add(flow);
        }

        @Override
        public void visit(FencedCodeBlock codeBlock) {
            flushBlock();

            String lang = codeBlock.getInfo() != null ? codeBlock.getInfo() : "";
            String code = codeBlock.getLiteral();

            if (!lang.isEmpty()) {
                Label langLabel = new Label(lang);
                langLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;"
                        + " -fx-font-family: " + FONT_SANS + "; -fx-padding: 0 0 2 0;");
                getChildren().add(langLabel);
            }

            Label codeLabel = new Label(code);
            codeLabel.setWrapText(true);
            codeLabel.setMaxWidth(Double.MAX_VALUE);
            codeLabel.setStyle(
                    "-fx-background-color: " + COLOR_CODE_BG + ";"
                            + " -fx-text-fill: " + COLOR_CODE_TEXT + ";"
                            + " -fx-font-family: " + FONT_MONO + ";"
                            + " -fx-font-size: 13px;"
                            + " -fx-padding: 8 12;"
                            + " -fx-background-radius: 6;"
                            + " -fx-line-spacing: 1.4;");
            getChildren().add(codeLabel);
        }

        @Override
        public void visit(IndentedCodeBlock codeBlock) {
            flushBlock();

            Label codeLabel = new Label(codeBlock.getLiteral());
            codeLabel.setWrapText(true);
            codeLabel.setMaxWidth(Double.MAX_VALUE);
            codeLabel.setStyle(
                    "-fx-background-color: " + COLOR_CODE_BG + ";"
                            + " -fx-text-fill: " + COLOR_CODE_TEXT + ";"
                            + " -fx-font-family: " + FONT_MONO + ";"
                            + " -fx-font-size: 13px;"
                            + " -fx-padding: 8 12;"
                            + " -fx-background-radius: 6;"
                            + " -fx-line-spacing: 1.4;");
            getChildren().add(codeLabel);
        }

        @Override
        public void visit(BulletList bulletList) {
            flushBlock();
            visitChildren(bulletList);
        }

        @Override
        public void visit(OrderedList orderedList) {
            flushBlock();

            int startNum = orderedList.getStartNumber();
            int index = startNum;
            for (Node child = orderedList.getFirstChild();
                 child != null; child = child.getNext()) {
                renderListItem(child, index + ". ");
                index++;
            }
        }

        @Override
        public void visit(ListItem listItem) {
            renderListItem(listItem, "• ");
        }

        private void renderListItem(Node item, String bullet) {
            TextFlow flow = new TextFlow();
            flow.setStyle("-fx-padding: 1 0 1 16;");

            javafx.scene.text.Text bulletNode = new javafx.scene.text.Text(bullet);
            bulletNode.setStyle("-fx-fill: " + COLOR_QUOTE + ";");
            flow.getChildren().add(bulletNode);

            addInlineChildren(item, flow,
                    "-fx-fill: " + COLOR_TEXT + "; -fx-font-size: 14px;");
            getChildren().add(flow);
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            flushBlock();

            TextFlow flow = new TextFlow();
            flow.setStyle("-fx-border-color: " + COLOR_QUOTE_BORDER + ";"
                    + " -fx-border-width: 0 0 0 3;"
                    + " -fx-padding: 4 12;"
                    + " -fx-background-color: rgba(200,169,126,0.04);");

            addInlineChildren(blockQuote, flow,
                    "-fx-fill: " + COLOR_QUOTE + "; -fx-font-size: 14px;");
            getChildren().add(flow);
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            flushBlock();

            Label hr = new Label();
            hr.setStyle("-fx-background-color: #334155;"
                    + " -fx-pref-height: 1; -fx-max-height: 1;"
                    + " -fx-margin: 8 0;");
            hr.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(hr);
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            appendInline(new javafx.scene.text.Text("\n"), "");
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            appendInline(new javafx.scene.text.Text("\n"), "");
        }

        @Override
        public void visit(Image image) {
            // TextFlow 无法显示图片，渲染为占位文本
            String alt = extractText(image);
            javafx.scene.text.Text placeholder = new javafx.scene.text.Text(
                    "[" + (alt.isEmpty() ? "Image" : alt) + "]");
            placeholder.setStyle("-fx-fill: " + COLOR_LINK + ";"
                    + " -fx-font-style: italic;");
            appendInline(placeholder, null);
        }

        // ── 行内元素（在段落 TextFlow 中添加） ──

        @Override
        public void visit(org.commonmark.node.Text text) {
            appendInline(new javafx.scene.text.Text(text.getLiteral()),
                    "-fx-fill: " + COLOR_TEXT + ";");
        }

        @Override
        public void visit(StrongEmphasis emphasis) {
            appendInlineText(emphasis,
                    "-fx-font-weight: bold; -fx-fill: " + COLOR_HEADING + ";");
        }

        @Override
        public void visit(Emphasis emphasis) {
            appendInlineText(emphasis,
                    "-fx-font-style: italic; -fx-fill: " + COLOR_QUOTE + ";");
        }

        @Override
        public void visit(Code code) {
            javafx.scene.text.Text node = new javafx.scene.text.Text(code.getLiteral());
            node.setStyle("-fx-font-family: " + FONT_MONO + ";"
                    + " -fx-fill: " + COLOR_CODE_TEXT + ";"
                    + " -fx-font-size: 13px;");
            appendInline(node, null);
        }

        @Override
        public void visit(Link link) {
            javafx.scene.text.Text node = new javafx.scene.text.Text(extractText(link));
            node.setStyle("-fx-fill: " + COLOR_LINK + ";"
                    + " -fx-underline: true;");
            appendInline(node, null);
        }

        // ── 辅助方法 ──

        private void flushBlock() {
            if (currentBlock != null) {
                getChildren().add(currentBlock);
                currentBlock = null;
            }
        }

        private void appendInline(javafx.scene.text.Text text, String style) {
            if (currentBlock == null) {
                currentBlock = createParagraphFlow();
            }
            if (style != null) {
                text.setStyle(style);
            }
            currentBlock.getChildren().add(text);
        }

        private void appendInlineText(Node source, String style) {
            javafx.scene.text.Text text = new javafx.scene.text.Text(extractText(source));
            appendInline(text, style);
        }

        /**
         * 将节点下的行内子元素渲染到指定 TextFlow。
         * 用于列表项、引用等需要自定义容器样式的场景。
         */
        private void addInlineChildren(Node parent, TextFlow target,
                                       String baseStyle) {
            for (Node child = parent.getFirstChild();
                 child != null; child = child.getNext()) {
                if (child instanceof org.commonmark.node.Text textNode) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(textNode.getLiteral());
                    t.setStyle(baseStyle);
                    target.getChildren().add(t);
                } else if (child instanceof StrongEmphasis strong) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(extractText(strong));
                    t.setStyle(baseStyle + " -fx-font-weight: bold;");
                    target.getChildren().add(t);
                } else if (child instanceof Emphasis em) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(extractText(em));
                    t.setStyle(baseStyle + " -fx-font-style: italic;");
                    target.getChildren().add(t);
                } else if (child instanceof Code code) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(code.getLiteral());
                    t.setStyle("-fx-font-family: " + FONT_MONO + ";"
                            + " -fx-fill: " + COLOR_CODE_TEXT + ";"
                            + " -fx-font-size: 13px;");
                    target.getChildren().add(t);
                } else if (child instanceof Link link) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(extractText(link));
                    t.setStyle("-fx-fill: " + COLOR_LINK + "; -fx-underline: true;");
                    target.getChildren().add(t);
                } else if (child instanceof Image image) {
                    String alt = extractText(image);
                    javafx.scene.text.Text t = new javafx.scene.text.Text(
                            "[" + (alt.isEmpty() ? "Image" : alt) + "]");
                    t.setStyle("-fx-fill: " + COLOR_LINK + "; -fx-font-style: italic;");
                    target.getChildren().add(t);
                } else if (child instanceof SoftLineBreak
                        || child instanceof HardLineBreak) {
                    target.getChildren().add(new javafx.scene.text.Text("\n"));
                } else {
                    addInlineChildren(child, target, baseStyle);
                }
            }
        }

        /**
         * 递归提取节点下所有纯文本内容。
         */
        private String extractText(Node node) {
            if (node instanceof org.commonmark.node.Text textNode) {
                return textNode.getLiteral();
            }
            StringBuilder sb = new StringBuilder();
            for (Node child = node.getFirstChild();
                 child != null; child = child.getNext()) {
                sb.append(extractText(child));
            }
            return sb.toString();
        }
    }

    private static TextFlow createParagraphFlow() {
        TextFlow flow = new TextFlow();
        flow.setStyle("-fx-padding: 0;");
        return flow;
    }
}
