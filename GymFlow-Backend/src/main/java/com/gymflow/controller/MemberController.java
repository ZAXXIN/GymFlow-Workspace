package com.gymflow.controller;

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
import com.gymflow.common.Result;

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
    @Operation(summary = "分页查询会员列表", description = "根据条件分页查询会员列表")
    public Result<PageResultVO<MemberListVO>> getMemberList(@Valid @RequestBody MemberQueryDTO queryDTO) {
        try {
            PageResultVO<MemberListVO> result = memberService.getMemberList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询会员列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{memberId}")
    @Operation(summary = "获取会员详情", description = "根据会员ID获取会员详细信息")
    public Result<MemberFullDTO> getMemberDetail(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        try {
            MemberFullDTO detail = memberService.getMemberDetail(memberId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取会员详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "添加会员", description = "添加新会员，包含基本信息、健康档案和会员卡信息")
    public Result<Long> addMember(@Valid @RequestBody MemberAddRequest request) {
        try {
            Long memberId = memberService.addMember(
                    request.getBasicDTO(),
                    request.getHealthRecordDTO(),
                    request.getCardDTO()
            );
            return Result.success("添加成功", memberId);
        } catch (Exception e) {
            log.error("添加会员失败：{}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{memberId}")
    @Operation(summary = "更新会员信息", description = "更新会员的基本信息、健康档案和会员卡信息")
    public Result<Void> updateMember(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        try {
            memberService.updateMember(
                    memberId,
                    request.getBasicDTO(),
//                    request.getHealthRecordDTO(),
                    request.getCardDTO()
            );
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新会员失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{memberId}")
    @Operation(summary = "删除会员", description = "根据会员ID删除会员（软删除）")
    public Result<Void> deleteMember(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        try {
            memberService.deleteMember(memberId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除会员失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除会员", description = "批量删除多个会员")
    public Result<Void> batchDeleteMember(@RequestBody List<Long> memberIds) {
        try {
            memberService.batchDeleteMember(memberIds);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除会员失败：{}", e.getMessage(), e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/renew-card/{memberId}")
    @Operation(summary = "续费会员卡", description = "为会员续费会员卡")
    public Result<Void> renewMemberCard(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody MemberCardDTO cardDTO) {
        try {
            memberService.renewMemberCard(memberId, cardDTO);
            return Result.success("续费成功");
        } catch (Exception e) {
            log.error("续费会员卡失败：{}", e.getMessage(), e);
            return Result.error("续费失败：" + e.getMessage());
        }
    }

    @GetMapping("/health-records/{memberId}")
    @Operation(summary = "获取健康档案列表", description = "获取会员的健康档案记录列表")
    public Result<List<HealthRecordDTO>> getHealthRecords(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId) {
        try {
            List<HealthRecordDTO> records = memberService.getHealthRecords(memberId);
            return Result.success("查询成功", records);
        } catch (Exception e) {
            log.error("获取健康档案失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/add-health-record/{memberId}")
    @Operation(summary = "添加健康档案", description = "为会员添加新的健康档案记录")
    public Result<Void> addHealthRecord(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody HealthRecordDTO healthRecordDTO) {
        try {
            memberService.addHealthRecord(memberId, healthRecordDTO);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加健康档案失败：{}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }
}

// 请求包装类
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