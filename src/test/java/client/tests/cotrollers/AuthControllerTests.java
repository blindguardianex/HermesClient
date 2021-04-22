package client.tests.cotrollers;

import client.model.utils.StageShower;
import org.junit.Before;

import java.io.IOException;

public class AuthControllerTests {

    @Before
    public void initController() throws IOException {
        StageShower.getShower("/Authorization.fxml").show();

    }
}
