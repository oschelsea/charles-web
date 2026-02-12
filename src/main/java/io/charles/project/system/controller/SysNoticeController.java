package io.charles.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charles.framework.aspectj.lang.annotation.Log;
import io.charles.framework.aspectj.lang.enums.BusinessType;
import io.charles.framework.web.controller.BaseController;
import io.charles.framework.web.domain.R;
import io.charles.framework.web.page.TableDataInfo;
import io.charles.project.system.domain.SysNotice;
import io.charles.project.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告 信息操作处理
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    private final ISysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice) {
        Page<SysNotice> page = getPage();
        noticeService.selectNoticeList(page, notice);
        return getDataTable(page);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public R<SysNotice> getInfo(@PathVariable Long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysNotice notice) {
        notice.setCreateBy(getUsername());
        return toResult(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysNotice notice) {
        notice.setUpdateBy(getUsername());
        return toResult(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        return toResult(noticeService.deleteNoticeByIds(noticeIds));
    }
}
