package io.charles.project.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charles.common.utils.poi.ExcelUtil;
import io.charles.framework.aspectj.lang.annotation.Log;
import io.charles.framework.aspectj.lang.enums.BusinessType;
import io.charles.framework.web.controller.BaseController;
import io.charles.framework.web.domain.R;
import io.charles.framework.web.page.TableDataInfo;
import io.charles.project.monitor.domain.SysOperLog;
import io.charles.project.monitor.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {
    private final ISysOperLogService operLogService;

    @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysOperLog operLog) {
        Page<SysOperLog> page = getPage();
        operLogService.selectOperLogList(page, operLog);
        return getDataTable(page);
    }

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:export')")
    @PostMapping("/export")
    public void export(SysOperLog operLog, HttpServletResponse response) {
        List<SysOperLog> list = operLogService.selectOperLogList(null, operLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        util.exportExcel(response, list, "操作日志");
    }

    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable Long[] operIds) {
        return toResult(operLogService.deleteOperLogByIds(operIds));
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
