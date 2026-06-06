import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import com.alibaba.dashscope.utils.JsonUtils;

public class Text2Image {

    static String apiKey = System.getenv("DASHSCOPE_API_KEY");

    public static void basicCall() throws ApiException, NoApiKeyException {
        String prompt = "**Thinking:**\n" +
                "```\n" +
                "用户现在需要描述这幅插画。首先看整体风格是动漫、数字绘画，氛围偏暗沉又带柔和光影。先看人物：女性角色，短发（深紫色或黑色，带深色挑染），侧分刘海。穿着黑色无袖长裙，露肩设计，腰间有细带。鞋子是黑色短靴，厚底，带系带细节。\n" +
                "\n" +
                "姿势：坐在类似平台或矮墙，腿部交叉，一只腿抬起，手托下巴，另一只手搭腿，姿势放松但略带沉思。背景是深色调，可能是室内，深蓝色和黑色为主，光线从左上方或右侧打来，营造阴影和高光，人物有柔和的暖光，增加氛围感。\n" +
                "\n" +
                "面部表情：眼神看向下方或一侧，表情平静、思考，嘴唇轻抿。整体色彩：深色系（黑、蓝、灰）但人物有暖光，对比突出。艺术风格是写实与动漫结合，注重光影和人物造型，适合角色设计类作品。\n" +
                "\n" +
                "现在组织语言：这幅数字插画描绘了一位动漫风格的女性角色。她留着深色（带紫色挑染）的中短发，侧分刘海，眼神低垂略带沉思，嘴唇轻抿，神态平静。角色身穿黑色无袖长裙，露肩设计贴合身形，腰间细带增添层次；脚蹬黑色厚底短靴，靴面有系带细节，强化硬朗风格。她坐在一处深色平台，双腿交叠，右手托腮、左手搭腿，姿态慵懒又透着思考感。背景以深蓝和黑色为主，左侧有模糊的竖向线条（可能窗框或墙缝），光线从侧面打来（暖光打在人物腿部、肩颈），营造出光影交错的效果，烘托出略带神秘与沉静的氛围，整体色彩对比鲜明，写实与动漫风格融合，注重光影表现和人物神态刻画，给人文艺又带点疏离感的观感。\n" +
                "```\n" +
                "\n" +
                "**Response:**\n" +
                "This is a digital illustration of an anime - style female character, rendered in a blend of realism and anime aesthetics. Here's a detailed breakdown:  \n" +
                "\n" +
                "### Character Design  \n" +
                "- **Appearance**: She has short, dark - colored hair (with subtle purple highlights) styled in a shaggy bob. The part is cut to the side, and her eyes, which are brown, gaze downward with a pensive expression. Her mouth is slightly pursed, contributing to a calm and contemplative air.  \n" +
                "- **Outfit**: She wears a sleeveless black dress with a plunging neckline that emphasizes her shoulders. The dress has a delicate belt at the waist. On her feet, she sports **black chunky ankle boots** with lace detailing and a thick heel, which add a touch of edginess to her overall look.  \n" +
                "\n" +
                "\n" +
                "### Pose and Setting  \n" +
                "- **Pose**: She sits casually on what seems to be a dark platform (possibly a bench or a ledge). Her legs are crossed, with one leg elevated. Her right hand rests under her chin, while her left hand lightly touches the raised leg, giving off a relaxed yet thoughtful vibe.  \n" +
                "- **Background**: The backdrop is dominated by dark hues of deep blue and black, evoking the atmosphere of an indoor space or a night scene. Faint vertical lines (likely part of window frames or walls) add depth. The lighting comes from an unseen source on one side, casting soft, warm highlights on her skin, the fabric of her dress, and the boots. This interplay of light and shadow enhances the moody and atmospheric quality of the scene.  \n" +
                "\n" +
                "\n" +
                "### Artistic Style and Mood  \n" +
                "The illustration combines realistic shading and lighting with the fluid, expressive lines typical of anime. The color palette, focused on dark tones with warm accents, creates a somber yet intimate mood. The character’s contemplative expression and the soft, directional lighting make the image feel introspective and slightly mysterious.  \n" +
                "\n" +
                "\n" +
                "Overall, the artwork masterfully balances stylized anime features with realistic lighting and anatomy, capturing both the character’s personality and a subtle emotional tone.";
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .model("qwen-image")
                        .prompt(prompt)
                        .n(1)
                        .size("1328*1328")
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            throw new RuntimeException(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(result));
    }

    public static void listTask() throws ApiException, NoApiKeyException {
        ImageSynthesis is = new ImageSynthesis();
        AsyncTaskListParam param = AsyncTaskListParam.builder().build();
        ImageSynthesisListResult result = is.list(param);
        System.out.println(result);
    }

    public void fetchTask() throws ApiException, NoApiKeyException {
        String taskId = "your task id";
        ImageSynthesis is = new ImageSynthesis();
        // If set DASHSCOPE_API_KEY environment variable, apiKey can null.
        ImageSynthesisResult result = is.fetch(taskId, null);
        System.out.println(result.getOutput());
        System.out.println(result.getUsage());
    }

    public static void main(String[] args){
        try{
            basicCall();
            //listTask();
        }catch(ApiException|NoApiKeyException e){
            System.out.println(e.getMessage());
        }
    }
}