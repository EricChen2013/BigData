package cn.neu.hadoop.bigdata;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class CharacterRelationshipAnalysis {
    @Autowired
    private HadoopTemplate hadoopTemplate;
    @Value("${hadoop.namespace:/}")
    private String namespace;

    @Test
    public void all_mission_test() {
        // 任务初始化
        int cur_mission = 0;
        String source_data_path = "C:\\tem\\data";
        String input_path = namespace + "/input/";
        String output_path_format = namespace + "output";
        //hadoopTemplate.uploadDir(source_data_path, input_path);

        try {
            NameSplit.main(input_path, output_path_format + cur_mission);
            cur_mission++;
            NameCount.main(input_path, output_path_format + cur_mission);
            cur_mission++;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
}
