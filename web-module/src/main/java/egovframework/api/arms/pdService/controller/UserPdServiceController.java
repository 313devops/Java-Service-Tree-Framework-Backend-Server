package egovframework.api.arms.pdService.controller;

import egovframework.api.arms.menu.vo.MenuDTO;
import egovframework.api.arms.pdService.model.PdServiceDTO;
import egovframework.api.arms.pdService.service.PdService;
import egovframework.com.ext.jstree.springHibernate.core.controller.SHVAbstractController;
import egovframework.com.utl.fcc.service.EgovFileUploadUtil;
import egovframework.com.utl.fcc.service.EgovFormBasedFileVo;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping(value = {"/auth-user/api/arms/pdService"})
public class UserPdServiceController extends SHVAbstractController<PdService, PdServiceDTO> {

    @Autowired
    @Qualifier("PdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setJsTreeHibernateService(pdService);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Long ROOT_NODE_ID = new Long(2);
    private static final String NODE_TYPE = new String("default");

    @ResponseBody
    @RequestMapping(value = "/addEndNodeByRoot.do", method = RequestMethod.POST)
    public ModelAndView addEndNodeByRoot(PdServiceDTO pdServiceDTO,
                                BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new RuntimeException();

        //?????? ????????? ???????????? ???????????? ??????
        PdServiceDTO paramPdServiceDTO = new PdServiceDTO();
        paramPdServiceDTO.setWhere("c_parentid", ROOT_NODE_ID);
        List<PdServiceDTO> list = pdService.getChildNode(paramPdServiceDTO);

        //????????? ????????? maxPosition??? ?????????.
        PdServiceDTO maxPositionPdServiceDTO = list
                .stream()
                .max(Comparator.comparing(PdServiceDTO::getC_position))
                .orElseThrow(NoSuchElementException::new);

        logger.info("=======" + maxPositionPdServiceDTO);

        //?????? ??? ??????
        pdServiceDTO.setRef(ROOT_NODE_ID);
        pdServiceDTO.setC_position(maxPositionPdServiceDTO.getC_position() + 1);
        pdServiceDTO.setC_type(NODE_TYPE);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdService.addNode(pdServiceDTO));

        return modelAndView;
    }

    /**
     * ????????? Upload??? ????????????.
     *
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/uploadFileToNode.do", method=RequestMethod.POST)
    public String uploadFileToNode(HttpServletRequest request, Model model) throws Exception {
        // Spring multipartResolver ????????? ??? (commons-fileupload ??????)
        //List<EgovFormBasedFileVo> list = EgovFormBasedFileUtil.uploadFiles(request, uploadDir, maxFileSize);

        // Spring multipartResolver ?????????
        String uploadDir = "";
        long maxFileSize = new Long(313);
        List<EgovFormBasedFileVo> list = EgovFileUploadUtil.uploadFiles(request, uploadDir, maxFileSize);

        if (list.size() > 0) {
            EgovFormBasedFileVo vo = list.get(0);    // ????????? ?????????

            String url = request.getContextPath()
                    + "/utl/web/imageSrc.do?"
                    + "path=" + vo.getServerSubPath()
                    + "&physical=" + vo.getPhysicalName()
                    + "&contentType=" + vo.getContentType();

            //model.addAttribute("CKEditorFuncNum", request.getParameter("CKEditorFuncNum"));
            model.addAttribute("url", url);
        }

        return "egovframework/com/utl/wed/EgovInsertImage";
    }

}
