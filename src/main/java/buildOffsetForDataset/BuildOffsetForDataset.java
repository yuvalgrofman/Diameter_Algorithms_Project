package buildOffsetForDataset;

import it.unimi.dsi.webgraph.BVGraph;

public class BuildOffsetForDataset {
    private final String dataSetPath;

    public BuildOffsetForDataset(final String dataSetPath) {
        if (dataSetPath.charAt(dataSetPath.length() - 1) == '/') {
            this.dataSetPath = dataSetPath;
        }
        else {
            this.dataSetPath = dataSetPath + '/';
        }
    }

    /**
     * builds a dataset that was already downloaded from the webgraph datasets.
     * @param basename - the basename of the dataset.
     * @return true on success, and false otherwise.
     */
    private boolean buildOffset(String basename) {
        try {
            BVGraph.main(new String[]{"-o", "-O", "-L", dataSetPath + basename + "/" + basename});
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * build datasets from the webgraph datasets.
     * @param basenames - the basenames of the datasets.
     * @return true if building all datasets was done successfully, and false otherwise.
     */
    public boolean buildOffset(String[] basenames) {
        boolean flag = true;
        for (String basename : basenames) {
            if (!buildOffset(basename)) {
                System.out.println("failed at basename: " + basename);
                flag = false;
            }
        }

        return flag;
    }
}
