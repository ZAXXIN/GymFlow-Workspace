package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.member.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.gymflow.service.MemberService;
import com.gymflow.vo.MemberListVO;
import com.gymflow.vo.PageResultVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/member")
@Tag(name = "会员管理", description = "会员管理相关接口")
@Validated
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/list")
    @Operation(summary = "分页查询会员列表")
    @PreAuthorize("member:view")  // 查看权限（老板和前台都有）
    public Result<PageResultVO<MemberListVO>> getMemberList(@Valid @RequestBody MemberQueryDTO queryDTO) {
        PageResultVO<MemberListVO> result = memberService.getMemberList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/detail/{memberId}")
    @Operation(summary = "获取会员详情")
    @PreAuthorize("member:detail")  // 查看详情权限（老板和前台都有）
    public Result<MemberFullDTO> getMemberDetail(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        MemberFullDTO detail = memberService.getMemberDetail(memberId);
        return Result.success("查询成功", detail);
    }

    @PostMapping("/add")
    @Operation(summary = "添加会员")
    @PreAuthorize("member:add")  // 新增权限（老板和前台都有）
    public Result<Long> addMember(@Valid @RequestBody MemberAddRequest request) {
        Long memberId = memberService.addMember(
                request.getBasicDTO(),
                request.getHealthRecordDTO(),
                request.getCardDTO()
        );
        return Result.success("添加成功", memberId);
    }

    @PutMapping("/update/{memberId}")
    @Operation(summary = "更新会员信息")
    @PreAuthorize("member:edit")  // 编辑权限（老板和前台都有）
    public Result<Void> updateMember(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        memberService.updateMember(
                memberId,
                request.getBasicDTO(),
                request.getCardDTO()
        );
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{memberId}")
    @Operation(summary = "删除会员")
    @PreAuthorize("member:delete")  // 删除权限（只有老板有）
    public Result<Void> deleteMember(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        memberService.deleteMember(memberId);
        return Result.success("删除成功");
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除会员")
    @PreAuthorize("member:batch:delete")  // 批量删除权限（只有老板有）
    public Result<Void> batchDeleteMember(@RequestBody List<Long> memberIds) {
        memberService.batchDeleteMember(memberIds);
        return Result.success("批量删除成功");
    }

    @PostMapping("/renew-card/{memberId}")
    @Operation(summary = "续费会员卡")
    @PreAuthorize("member:card:renew")  // 续费权限（老板和前台都有）
    public Result<Void> renewMemberCard(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody MemberCardDTO cardDTO) {
        memberService.renewMemberCard(memberId, cardDTO);
        return Result.success("续费成功");
    }

    @GetMapping("/health-records/{memberId}")
    @Operation(summary = "获取健康档案列表")
    @PreAuthorize("member:health:view")  // 查看健康档案权限（老板和前台都有）
    public Result<List<HealthRecordDTO>> getHealthRecords(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        List<HealthRecordDTO> records = memberService.getHealthRecords(memberId);
        return Result.success("查询成功", records);
    }

    @PostMapping("/add-health-record/{memberId}")
    @Operation(summary = "添加健康档案")
    @PreAuthorize("member:health:add")  // 添加健康档案权限（老板和前台都有）
    public Result<Void> addHealthRecord(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody HealthRecordDTO healthRecordDTO) {
        memberService.addHealthRecord(memberId, healthRecordDTO);
        return Result.success("添加成功");
    }
}

// 请求包装类（保持不变）
@Data
class MemberAddRequest {
    @Valid
    private MemberBasicDTO basicDTO;
    private HealthRecordDTO healthRecordDTO;
    private MemberCardDTO cardDTO;
}

@Data
class MemberUpdateRequest {
    @Valid
    private MemberBasicDTO basicDTO;
    private HealthRecordDTO healthRecordDTO;
    private MemberCardDTO cardDTO;
}