package org.onap.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ModelController {

    @RequestMapping("index")
    public String index(){
        return "index";
    }

}
