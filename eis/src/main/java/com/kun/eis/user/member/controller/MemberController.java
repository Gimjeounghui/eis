package com.kun.eis.user.member.controller;


import com.kun.eis.user.member.vo.MemberVO;
import com.kun.eis.common.util.BoardUtil;
import com.kun.eis.user.member.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;


@Controller
@RequestMapping("/member")
public class MemberController {

    /**
     * 23.04.28
     * memberLogin, memberInsert,forget_pw memberRegist, memberList, memberDetail, memberUpdate, memberDelete 추가
     */

    /**
     * 23.04.28
     * @Autowired
     * private MemberService memberService;
     */

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @RequestMapping(value="/memberList")
    public String memberList(@ModelAttribute("searchVO") MemberVO vo, Model model, HttpSession session) {

        // 게시판 UTIL
        BoardUtil boardUtil = new BoardUtil();
        List<MemberVO> memberList = null;

        try {

            /**
             * 게시판 기능
             */
            int totalRecordCount = 0; 								// 총 게시물 건수
            int currentPageNo = vo.getCurrentPageNo(); 				// 현재 클릭한 page번호
            int pageSize = vo.getPageSize(); 						// 페이지 리스트에 게시되는 페이지 건수
            int recordCountPerPage = vo.getRecordCountPerPage();	// 한 페이지당 게시되는 게시물 건 수

            // 게시물 조회 범위 연산
            HashMap<String, Integer> rangeMap = boardUtil.calcDataRange(currentPageNo, recordCountPerPage);
            vo.setStart(rangeMap.get("firstRecordIndex"));
            vo.setEnd(rangeMap.get("lastRecordIndex"));

            // 전체 검색 결과
             memberList = memberService.MemberList(vo);   //memberList 생성

            // 검색된 결과가 있는지 체크
            if(memberList.size() > 0) { // memberList의 데이터가 존재 할 때
                // 전체 검색 결과 게시물 건 수
                totalRecordCount = memberList.get(0).getTotalRecordCount(); //get쌧미
            }

            // pager기능 모든 계산식 결과 정보 map에 담기
            HashMap<String, Integer> pagerMap = boardUtil.calcBoardPagerElement(currentPageNo, totalRecordCount, recordCountPerPage, pageSize);

            // model 세팅
            model.addAttribute("memberList", memberList);
            model.addAttribute("pagerMap", pagerMap);

        } catch (Exception e) {

            logger.info(e.getMessage());
            e.printStackTrace();
        }

        return "/member/memberList";
    }
    
    
    @RequestMapping("/memberLogin")
    public String memberLogin(@ModelAttribute("MemberVO") MemberVO vo, Model model,
                            HttpSession session, HttpServletRequest request) {
        String email = request.getParameter("m_email");
        String password = request.getParameter("m_pw");

        /**
         *  1. Memberloginfind(이메일, 비밀번호 일치 확인 ), Memberpermiss(회원 가입 승인 확인) 생성
         *  2. Memberpermiss, memberloginfind가 false 라면 로그인 거부
         *    1) return "/member/login";
         *  3. Memberpermiss, memberloginfind가 true 라면 로그인 승인
         *  */

        // 로그인 성공시 홈페이지로 리다이렉트
        return "/member/home";
    }

   @RequestMapping("/singUp")
    public String membersingUp() {

       return "/member/memberRegist";
   }

    @RequestMapping("/forget_pw")
    public String pwFind(){

        return "forget_pw";
    }

   @RequestMapping("/memberRegist")
    public String memberRegist(@ModelAttribute("MemberVO") MemberVO vo, Model model, HttpSession session){

        return "/member/memberRegist";
    }


    @RequestMapping(value="/memberForm", method=RequestMethod.POST)
    public String memberForm(MemberVO vo) {

        boolean a = memberService.memberRegist(vo);
        return "/member/memberLogin";
    }


    /* memberList ver1
    @RequestMapping("/memberList")
    public String memberList(@ModelAttribute("MemberVO") MemberVO vo, Model model, HttpSession session) {
        return "memberList";
    }
    */


    @RequestMapping("/memberDetail")
    public String memberDetail(@ModelAttribute("MemberVO") MemberVO vo, Model model,
                               HttpSession session, @RequestParam("m_email") String m_email){
        vo = memberService.memberDetail(m_email);
        model.addAttribute("MemberVO", vo);

        return "memberDetail";
    }


    @Transactional
    @RequestMapping("/memberUpdate")                                // MemberVO에 m_photo 추가 후 수정 - 완료
    public String MemberUpdate(@ModelAttribute ("MemberVO") MemberVO vo,Model model, HttpSession session_a,
                               @RequestParam("m_photo1") MultipartFile m_photo,RedirectAttributes ra, HttpServletRequest request) {

        session_a.invalidate();
        HttpSession session = request.getSession();
        boolean a = memberService.memberUpdate(vo, m_photo);
        MemberVO member = vo;
        session.setAttribute("member",member);
        ra.addAttribute("m_email", vo.getM_email());

        /**
         * 23.04.28
         * info = 멤버정보(M_info) / 마이페이지(M_page)
         * 네이밍은 차후 변경 가능
         */

        return "redirect:/info";
    }

    @Transactional
    @RequestMapping("/memberDelete")
    public String memberDelete(@RequestParam("m_email") String m_email, RedirectAttributes ra, HttpSession session){

        /**
         * 23.04.28
         * MemberVO 완성 후 memberDelete 추가
         * 23.04.28 임시 MemberVO 생성 후 추가 완료
         */
        boolean b = memberService.memberDelete(m_email);
        if(b) {
            ra.addFlashAttribute("msg", "탈퇴가 완료되었습니다.");
        } else {
            ra.addFlashAttribute(("msg"), "오류로 인하여 탈퇴가 실패하였습니다.");
        }
        session.invalidate();
        return "redirect:/login";
    }

}
