package de.bonndan.nivio.output.jgraphx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import de.bonndan.nivio.input.ItemDescriptionFormatFactory;
import de.bonndan.nivio.input.LandscapeDescriptionFactory;
import de.bonndan.nivio.input.Indexer;
import de.bonndan.nivio.input.dto.GroupDescription;
import de.bonndan.nivio.input.dto.ItemDescription;
import de.bonndan.nivio.input.dto.LandscapeDescription;
import de.bonndan.nivio.input.nivio.ItemDescriptionFactoryNivio;
import de.bonndan.nivio.model.LandscapeImpl;
import de.bonndan.nivio.model.LandscapeRepository;
import de.bonndan.nivio.notification.NotificationService;
import de.bonndan.nivio.util.RootPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class JGraphXRendererTest {

    private LandscapeRepository landscapeRepository;
    private ItemDescriptionFormatFactory formatFactory;
    private Indexer indexer;

    @BeforeEach
    public void setup() {
        landscapeRepository = new LandscapeRepository();
        formatFactory = ItemDescriptionFormatFactory.with(ItemDescriptionFactoryNivio.forTesting());

        indexer = new Indexer(landscapeRepository, formatFactory, new NotificationService(null));
    }

    private LandscapeImpl getLandscape(String path) {
        File file = new File(RootPath.get() + path);
        LandscapeDescription landscapeDescription = LandscapeDescriptionFactory.fromYaml(file);
        indexer.reIndex(landscapeDescription);
        return landscapeRepository.findDistinctByIdentifier(landscapeDescription.getIdentifier()).orElseThrow();
    }

    private mxGraph debugRender(String path) throws IOException {
        LandscapeImpl landscape = getLandscape(path + ".yml");
        return debugRenderLandscape(path, landscape, true);
    }

    private mxGraph debugRenderLandscape(String path, LandscapeImpl landscape, boolean debugMode) throws IOException {
        JGraphXRenderer jGraphXRenderer = new JGraphXRenderer(null);
        jGraphXRenderer.setDebugMode(debugMode);

        mxGraph graph = jGraphXRenderer.render(landscape);

        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, null, true, null);
        assertNotNull(image);

        File png = new File(RootPath.get() + path + "_debug.png");
        ImageIO.write(image, "PNG", png);

        return graph;
    }

    @Test
    public void debugRenderExample() throws IOException {
        debugRender("/src/test/resources/example/example_env");
    }

    @Test
    public void debugRenderFourGroups() throws IOException {
        debugRender("/src/test/resources/example/example_four_groups");
    }

    @Test
    public void debugRenderInout() throws IOException {
        debugRender("/src/test/resources/example/inout");
    }

    @Test
    public void debugRenderLargeGraph() throws IOException {

        LandscapeDescription input = new LandscapeDescription();
        input.setIdentifier("largetest");
        input.setName("largetest");

        int g = 0;
        while (g < 30) {

            int i = 0;
            int max = g % 2 > 0 ? 5 :8;
            GroupDescription gd = new GroupDescription();
            String groupIdentifier = "group" + g;
            gd.setIdentifier(groupIdentifier);
            input.getGroups().put(groupIdentifier, gd);
            while (i < max) {
                ItemDescription itemDescription = new ItemDescription();
                itemDescription.setIdentifier(groupIdentifier + "_item_" + i);
                itemDescription.setGroup(groupIdentifier);
                input.getItemDescriptions().add(itemDescription);
                i++;
            }
            g++;
        }

        indexer.reIndex(input);
        LandscapeImpl landscape = landscapeRepository.findDistinctByIdentifier(input.getIdentifier()).orElseThrow();

        debugRenderLandscape("/src/test/resources/example/large", landscape, false);
    }

    @Test
    public void renderLandscapeItemModelWithMagicLabels() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ItemDescription model = new ItemDescription();
        model.setIdentifier("item");
        model.setName("Item Description");

        Map<String, Object> map = mapper.convertValue(model, Map.class);

        LandscapeDescription landscapeDescription = new LandscapeDescription();
        landscapeDescription.setIdentifier("landscapeItem:model");
        landscapeDescription.setName("Landscape Item Model");
        landscapeDescription.getItemDescriptions().add(model);

        map.forEach((field, o) -> {
            ItemDescription d = new ItemDescription();
            d.setIdentifier(field);
            landscapeDescription.getItemDescriptions().add(d);
            model.getLabels().put(field + "_PROVIDER_URL", field.toLowerCase());
        });

        indexer.reIndex(landscapeDescription);
        LandscapeImpl landscape = landscapeRepository.findDistinctByIdentifier(landscapeDescription.getIdentifier()).orElseThrow();

        debugRenderLandscape("/src/test/resources/example/model", landscape, false);
    }
}