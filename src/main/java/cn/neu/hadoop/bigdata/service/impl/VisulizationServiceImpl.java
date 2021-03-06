package cn.neu.hadoop.bigdata.service.impl;

import cn.neu.hadoop.bigdata.bean.echarts.EchartsOptionBar;
import cn.neu.hadoop.bigdata.bean.echarts.EchartsOptionGraph;
import cn.neu.hadoop.bigdata.bean.echarts.EchartsOptionPie;
import cn.neu.hadoop.bigdata.bean.echarts.EchartsOptionWordcloud;
import cn.neu.hadoop.bigdata.bean.echarts.common.*;
import cn.neu.hadoop.bigdata.bean.echarts.series.*;
import cn.neu.hadoop.bigdata.bean.echarts.series.graph.EchartsGraphCategory;
import cn.neu.hadoop.bigdata.bean.echarts.series.graph.EchartsGraphLink;
import cn.neu.hadoop.bigdata.bean.echarts.series.graph.EchartsGraphNode;
import cn.neu.hadoop.bigdata.bean.echarts.series.pie.EchartsPieData;
import cn.neu.hadoop.bigdata.bean.echarts.series.wordcloud.EchartsWordCloudAData;
import cn.neu.hadoop.bigdata.bean.echarts.series.wordcloud.EchartsWordCloudColor;
import cn.neu.hadoop.bigdata.bean.echarts.series.wordcloud.EchartsWordCloudItemStyle;
import cn.neu.hadoop.bigdata.bean.echarts.series.wordcloud.EchartsWordcloudAutoSize;
import cn.neu.hadoop.bigdata.hadoop.HadoopTemplate;
import cn.neu.hadoop.bigdata.service.VisulizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Service
public class VisulizationServiceImpl implements VisulizationService {
    @Autowired
    HadoopTemplate hadoopTemplate;


    @Override
    public EchartsOptionBar get_echarts_bar_json(String filepath) throws Exception {
        EchartsOptionBar echartsOptionBar = new EchartsOptionBar();

        EchartsTitle tem_title = new EchartsTitle();
        tem_title.setText(filepath + " Bar");
        tem_title.setShow(true);
        echartsOptionBar.setTitle(tem_title);
        echartsOptionBar.setTooltip(new EchartsTooltip());

        EchartsDataZoomX tem_data_zoom_x = new EchartsDataZoomX();
        EchartsDataZoomY tem_data_zoom_y = new EchartsDataZoomY();
        tem_data_zoom_x.setType("inside");
        tem_data_zoom_x.setxAxisIndex(0);
        tem_data_zoom_y.setType("inside");
        tem_data_zoom_y.setyAxisIndex(0);
        EchartsDataZoom[] tem_data_zoom_list = {tem_data_zoom_x, tem_data_zoom_y};
        echartsOptionBar.setDataZoom(tem_data_zoom_list);

        String[] words = hadoopTemplate.read(true, filepath).split("\n");
        List<String> xAxis_data = new LinkedList<>();
        List<Float> series_data = new LinkedList<>();
        for (String i : words) {
            String[] name_count = i.split("\t");
            xAxis_data.add(name_count[0]);
            series_data.add(Float.valueOf(name_count[1]));
        }
        EchartsxAxis tem_xAxis = new EchartsxAxis();
        String[] tem_xAixs_data_list = new String[xAxis_data.size()];
        xAxis_data.toArray(tem_xAixs_data_list);
        tem_xAxis.setData(tem_xAixs_data_list);
        echartsOptionBar.setxAxis(tem_xAxis);
        echartsOptionBar.setyAxis(new EchartsyAxis());

        EchartsBar tem_series = new EchartsBar();
        Float[] test = new Float[series_data.size()];
        series_data.toArray(test);
        tem_series.setName("次数");
        tem_series.setData(test);

        EchartsSeriesBase[] tem_series_list = {tem_series};
        echartsOptionBar.setSeries(tem_series_list);
        return echartsOptionBar;
    }

    @Override
    public EchartsOptionGraph get_echarts_graph_json(String filepath) throws Exception {
        EchartsOptionGraph echartsOptionGraph = new EchartsOptionGraph();

        EchartsTitle echartsTitle = new EchartsTitle();
        echartsTitle.setText(filepath + " Graph");
        echartsTitle.setShow(true);
        echartsOptionGraph.setTitle(echartsTitle);
        echartsOptionGraph.setTooltip(new EchartsTooltip());

        EchartsGraph echartsGraph = new EchartsGraph();
        echartsGraph.setLayout("force");
        echartsGraph.setRoam(true);
        echartsGraph.setFocusNodeAdjacency(true);

        List<EchartsGraphNode> tem_node_list = new LinkedList<>();
        List<EchartsGraphLink> tem_link_list = new LinkedList<>();
        String[] g_words = hadoopTemplate.read(true, filepath).split("\n");
        HashSet<Integer> all_category = new HashSet<>();
        for (String i : g_words) {
            String[] label_and_name_point_relationship = i.split("\t");
            String[] name_and_point_and_relationship = label_and_name_point_relationship[1].split("#");
            EchartsGraphNode tem_node = new EchartsGraphNode();
            tem_node.setName(name_and_point_and_relationship[0]);
            int category = Integer.parseInt(label_and_name_point_relationship[0]);
            tem_node.setCategory(category);
            all_category.add(category);
            float point = Float.parseFloat(name_and_point_and_relationship[1]);
            tem_node.setValue(point);
            tem_node.setSymbolSize(point);
            tem_node_list.add(tem_node);
            for (String j : name_and_point_and_relationship[2].split(";")) {
                String[] target_name_and_weight = j.split(":");
                EchartsGraphLink tem_link = new EchartsGraphLink();
                tem_link.setSource(name_and_point_and_relationship[0]);
                tem_link.setTarget(target_name_and_weight[0]);
                tem_link_list.add(tem_link);
            }
        }
        EchartsGraphNode[] tem_node_trans_list = new EchartsGraphNode[tem_node_list.size()];
        tem_node_list.toArray(tem_node_trans_list);
        echartsGraph.setNodes(tem_node_trans_list);
        EchartsGraphLink[] tem_link_trans_list = new EchartsGraphLink[tem_link_list.size()];
        tem_link_list.toArray(tem_link_trans_list);
        echartsGraph.setLinks(tem_link_trans_list);

        List<EchartsGraphCategory> tem_category_list = new LinkedList<>();
        for (int i = 0; i < all_category.size(); i++) {
            EchartsGraphCategory tem_category = new EchartsGraphCategory();
            tem_category.setName(String.valueOf(i));
            tem_category_list.add(tem_category);
        }
        EchartsGraphCategory[] tem_trans_category_list = new EchartsGraphCategory[tem_category_list.size()];
        tem_category_list.toArray(tem_trans_category_list);
        echartsGraph.setCategories(tem_trans_category_list);
        echartsOptionGraph.setSeries(new EchartsGraph[]{echartsGraph});
        return echartsOptionGraph;
    }

    @Override
    public EchartsOptionWordcloud get_echarts_wordcount_json(String filepath) throws Exception {
        EchartsOptionWordcloud echartsOptionWordcloud = new EchartsOptionWordcloud();

        EchartsTitle echartsTitle = new EchartsTitle();
        echartsTitle.setText(filepath + " WordCloud");
        echartsTitle.setShow(true);
        echartsOptionWordcloud.setTitle(echartsTitle);
        echartsOptionWordcloud.setTooltip(new EchartsTooltip());

        EchartsWordCloud echartsWordCloud = new EchartsWordCloud();
        echartsWordCloud.setAutoSize(new EchartsWordcloudAutoSize());
        String[] w_words = hadoopTemplate.read(true, filepath).split("\n");
        List<EchartsWordCloudAData> dataList = new LinkedList<>();
        for (int j = 0; j < 300 && j < w_words.length; j++) {
            String i = w_words[j];
            String[] value_word = i.split("\t");

            EchartsWordCloudAData tem_data = new EchartsWordCloudAData();
            tem_data.setName(value_word[1]);
            tem_data.setValue(Float.parseFloat(value_word[0]));
            EchartsWordCloudItemStyle echartsWordCloudItemStyle = new EchartsWordCloudItemStyle();
            EchartsWordCloudColor tem_color = new EchartsWordCloudColor();
            tem_color.setColor(String.format("rgb(%d,%d,%d)", Math.round(Math.random() * 255), Math.round(Math.random()
                    * 255), Math.round(Math.random() * 255)));
            echartsWordCloudItemStyle.setNormal(tem_color);
            tem_data.setItemStyle(echartsWordCloudItemStyle);
            dataList.add(tem_data);
        }
        EchartsWordCloudAData[] tem_trans_data_list = new EchartsWordCloudAData[dataList.size()];
        dataList.toArray(tem_trans_data_list);
        echartsWordCloud.setData(tem_trans_data_list);
        echartsOptionWordcloud.setSeries(new EchartsSeriesBase[]{echartsWordCloud});
        return echartsOptionWordcloud;
    }

    @Override
    public EchartsOptionPie get_echarts_pie_json(String filepath) throws Exception {
        EchartsOptionPie echartsOptionPie = new EchartsOptionPie();

        EchartsTitle echartsTitle = new EchartsTitle();
        echartsTitle.setText(filepath + "FundAnalysis");
        echartsTitle.setShow(true);
        echartsOptionPie.setTitle(echartsTitle);
        echartsOptionPie.setTooltip(new EchartsTooltip());

        EchartsPie echartsPie = new EchartsPie();
        String[] f_words = hadoopTemplate.read(true, filepath).split("\n");
        List<EchartsPieData> echartsPieDataList = new LinkedList<EchartsPieData>();
        for (String i : f_words) {
            String[] name_value = i.split("\t");
            EchartsPieData echartsPieData = new EchartsPieData();
            echartsPieData.setName(name_value[0] + " ~ " + (Integer.parseInt(name_value[0]) + 1));
            echartsPieData.setValue(name_value[1]);
            echartsPieDataList.add(echartsPieData);
        }
        EchartsPieData[] tem_trans_data_list = new EchartsPieData[echartsPieDataList.size()];
        echartsPieDataList.toArray(tem_trans_data_list);
        echartsPie.setData(tem_trans_data_list);

        echartsOptionPie.setSeries(new EchartsSeriesBase[]{echartsPie});
        return echartsOptionPie;
    }
}
