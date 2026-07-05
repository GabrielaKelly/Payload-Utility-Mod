package net.payload.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.*;
import net.payload.gui.colors.Color;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class PageComponent extends Component {
    private List<Page> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private float headerHeight = 30f;
    private StackPanelComponent contentPanel;

    public static class Page {
        private String name;
        private List<Component> components;

        public Page(String name) {
            this.name = name;
            this.components = new ArrayList<>();
        }

        public void addComponent(Component component) {
            components.add(component);
        }

        public List<Component> getComponents() {
            return components;
        }

        public String getName() {
            return name;
        }
    }

    public PageComponent(String header) {
        this.header = header;
        this.contentPanel = new StackPanelComponent();
        this.contentPanel.setVisible(true);
        super.addChild(contentPanel);
        this.setMargin(new Margin(8f, 4f, 8f, 4f));
    }

    public void addPage(Page page) {
        pages.add(page);
        if (pages.size() == 1) {
            updateVisibleComponents();
        }
    }

    private void updateVisibleComponents() {
        contentPanel.clearChildren();
        if (currentPageIndex >= 0 && currentPageIndex < pages.size()) {
            Page currentPage = pages.get(currentPageIndex);
            for (Component component : currentPage.getComponents()) {
                contentPanel.addChild(component);
            }
        }
    }

    private void nextPage() {
        currentPageIndex = (currentPageIndex + 1) % pages.size();
        updateVisibleComponents();
        invalidateMeasure();
    }

    @Override
    public void measure(Size availableSize) {
        contentPanel.measure(availableSize);
        float contentHeight = contentPanel.getPreferredSize().getHeight();
        preferredSize = new Size(availableSize.getWidth(), headerHeight + contentHeight);
    }

    @Override
    public void arrange(Rectangle finalSize) {
        super.arrange(finalSize);

        Rectangle contentBounds = new Rectangle(
                finalSize.getX(),
                finalSize.getY() + headerHeight,
                finalSize.getWidth(),
                finalSize.getHeight() - headerHeight
        );
        contentPanel.arrange(contentBounds);
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        MatrixStack matrixStack = drawContext.getMatrices();
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        float actualX = getActualSize().getX();
        float actualY = getActualSize().getY();
        float actualWidth = getActualSize().getWidth();

        Color baseColor = new Color(22, 22, 22, 100);
        Render2D.drawHorizontalGradient(matrix4f, actualX - 5, actualY + 6, actualWidth + 8, 30, baseColor, baseColor);

        if (!pages.isEmpty()) {
            Page currentPage = pages.get(currentPageIndex);

            // Draw the header text ("Page:")
            Render2D.drawString(
                    drawContext,
                    "Page:",
                    actualX + 1,
                    actualY + (headerHeight - 2) / 2,
                    0xFFFFFF
            );

            // Get the page info text
            String pageInfo = String.format("%s", currentPage.getName(), currentPageIndex + 1);
            float textWidth = Render2D.getStringWidth(pageInfo);

            float xOffset = 0;
            if (pageInfo.length() > 4) {
                xOffset = (pageInfo.length() - 4) * -4;
            }

            Render2D.drawString(
                    drawContext,
                    pageInfo,
                    actualX + (actualWidth / 2.0f) - textWidth + 61 + xOffset,
                    actualY + (headerHeight - 2) / 2,
                    hovered ? 0xFFFFFF : 0x9B9B9B
            );
        }

        contentPanel.draw(drawContext, partialTicks);
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        float mouseY = (float) event.mouseY;
        float headerY = getActualSize().getY();

        if (mouseY >= headerY && mouseY <= headerY + headerHeight) {
            if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
                nextPage();
                event.cancel();
                return;
            }
        }

        super.onMouseClick(event);
    }
}