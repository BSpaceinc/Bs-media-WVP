package com.genersoft.iot.vmp.vmanager.cloudRecord;

import com.alibaba.fastjson2.JSONArray;
import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.conf.security.JwtUtils;
import com.genersoft.iot.vmp.media.bean.MediaServer;
import com.genersoft.iot.vmp.service.ICloudRecordService;
import com.genersoft.iot.vmp.media.service.IMediaServerService;
import com.genersoft.iot.vmp.service.bean.CloudRecordItem;
import com.genersoft.iot.vmp.service.bean.DownloadFileInfo;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("rawtypes")
@Tag(name = "云端录像接口")

@RestController
@RequestMapping("/api/cloud/record")
public class CloudRecordController {


    private final static Logger logger = LoggerFactory.getLogger(CloudRecordController.class);

    @Autowired
    private ICloudRecordService cloudRecordService;

    @Autowired
    private IMediaServerService mediaServerService;


    @ResponseBody
    @GetMapping("/date/list")
    @Operation(summary = "查询存在云端录像的日期", security = @SecurityRequirement(name = JwtUtils.HEADER))
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @Parameter(name = "year", description = "年，置空则查询当年", required = false)
    @Parameter(name = "month", description = "月，置空则查询当月", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID，置空则查询全部", required = false)
    public List<String> openRtpServer(
            @RequestParam(required = true) String app,
            @RequestParam(required = true) String stream,
            @RequestParam(required = false) int year,
            @RequestParam(required = false) int month,
            @RequestParam(required = false) String mediaServerId

    ) {
        logger.info("[云端录像] 查询存在云端录像的日期 app->{}, stream->{}, mediaServerId->{}, year->{}, month->{}",
                app, stream, mediaServerId, year, month);
        Calendar calendar = Calendar.getInstance();
        if (ObjectUtils.isEmpty(year)) {
            year = calendar.get(Calendar.YEAR);
        }
        if (ObjectUtils.isEmpty(month)) {
            month = calendar.get(Calendar.MONTH) + 1;
        }
        List<MediaServer> mediaServerItems;
        if (!ObjectUtils.isEmpty(mediaServerId)) {
            mediaServerItems = new ArrayList<>();
            MediaServer mediaServerItem = mediaServerService.getOne(mediaServerId);
            if (mediaServerItem == null) {
                throw new ControllerException(ErrorCode.ERROR100.getCode(), "未找到流媒体: " + mediaServerId);
            }
            mediaServerItems.add(mediaServerItem);
        } else {
            mediaServerItems = mediaServerService.getAllOnlineList();
        }
        if (mediaServerItems.isEmpty()) {
            return new ArrayList<>();
        }

        return cloudRecordService.getDateList(app, stream, year, month, mediaServerItems);
    }

    @ResponseBody
    @GetMapping("/list")
    @Operation(summary = "分页查询云端录像", security = @SecurityRequirement(name = JwtUtils.HEADER))
    @Parameter(name = "query", description = "检索内容", required = false)
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "page", description = "当前页", required = true)
    @Parameter(name = "count", description = "每页查询数量", required = true)
    @Parameter(name = "startTime", description = "开始时间(yyyy-MM-dd HH:mm:ss)", required = false)
    @Parameter(name = "endTime", description = "结束时间(yyyy-MM-dd HH:mm:ss)", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID，置空则查询全部流媒体", required = false)
    public PageInfo<CloudRecordItem> openRtpServer(
            @RequestParam(required = false)  String query,
            @RequestParam(required = false)  String app,
            @RequestParam(required = false)  String stream,
            @RequestParam int page,
            @RequestParam int count,
            @RequestParam(required = false)  String startTime,
            @RequestParam(required = false)  String endTime,
            @RequestParam(required = false) String mediaServerId

    ) {
        logger.info("[云端录像] 查询 app->{}, stream->{}, mediaServerId->{}, page->{}, count->{}, startTime->{}, endTime->{}",
                app, stream, mediaServerId, page, count, startTime, endTime);

        List<MediaServer> mediaServerItems;
        if (!ObjectUtils.isEmpty(mediaServerId)) {
            mediaServerItems = new ArrayList<>();
            MediaServer mediaServerItem = mediaServerService.getOne(mediaServerId);
            if (mediaServerItem == null) {
                throw new ControllerException(ErrorCode.ERROR100.getCode(), "未找到流媒体: " + mediaServerId);
            }
            mediaServerItems.add(mediaServerItem);
        } else {
            mediaServerItems = mediaServerService.getAllOnlineList();
        }
        if (mediaServerItems.isEmpty()) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "当前无流媒体");
        }
        if (query != null && ObjectUtils.isEmpty(query.trim())) {
            query = null;
        }
        if (app != null && ObjectUtils.isEmpty(app.trim())) {
            app = null;
        }
        if (stream != null && ObjectUtils.isEmpty(stream.trim())) {
            stream = null;
        }
        if (startTime != null && ObjectUtils.isEmpty(startTime.trim())) {
            startTime = null;
        }
        if (endTime != null && ObjectUtils.isEmpty(endTime.trim())) {
            endTime = null;
        }

        return cloudRecordService.getList(page, count, query, app, stream, startTime, endTime, mediaServerItems);
    }

    @ResponseBody
    @GetMapping("/task/add")
    @Operation(summary = "添加合并任务")
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "startTime", description = "鉴权ID", required = false)
    @Parameter(name = "endTime", description = "鉴权ID", required = false)
    @Parameter(name = "callId", description = "鉴权ID", required = false)
    @Parameter(name = "remoteHost", description = "返回地址时的远程地址", required = false)
    public String addTask(
            HttpServletRequest request,
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream,
            @RequestParam(required = false) String mediaServerId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String callId,
            @RequestParam(required = false) String remoteHost
    ){
        MediaServer mediaServerItem;
        if (mediaServerId == null) {
            mediaServerItem = mediaServerService.getDefaultMediaServer();
        }else {
            mediaServerItem = mediaServerService.getOne(mediaServerId);
        }
        if (mediaServerItem == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "未找到可用的流媒体");
        }else {
            if (remoteHost == null) {
                remoteHost = request.getScheme() + "://" + mediaServerItem.getIp() + ":" + mediaServerItem.getRecordAssistPort();
            }
        }
        return cloudRecordService.addTask(app, stream, mediaServerItem, startTime, endTime, callId, remoteHost, mediaServerId != null);
    }

    @ResponseBody
    @GetMapping("/task/list")
    @Operation(summary = "查询合并任务")
    @Parameter(name = "taskId", description = "任务Id", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "isEnd", description = "是否结束", required = false)
    public JSONArray queryTaskList(
            HttpServletRequest request,
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream,
            @RequestParam(required = false) String callId,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String mediaServerId,
            @RequestParam(required = false) Boolean isEnd
    ){
       if (ObjectUtils.isEmpty(mediaServerId)) {
           mediaServerId = null;
       }

       return cloudRecordService.queryTask(app, stream, callId, taskId, mediaServerId, isEnd, request.getScheme());
    }

    @ResponseBody
    @GetMapping("/collect/add")
    @Operation(summary = "添加收藏")
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "startTime", description = "鉴权ID", required = false)
    @Parameter(name = "endTime", description = "鉴权ID", required = false)
    @Parameter(name = "callId", description = "鉴权ID", required = false)
    @Parameter(name = "recordId", description = "录像记录的ID，用于精准收藏一个视频文件", required = false)
    public int addCollect(
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream,
            @RequestParam(required = false) String mediaServerId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String callId,
            @RequestParam(required = false) Integer recordId
    ){
        logger.info("[云端录像] 添加收藏，app={}，stream={},mediaServerId={},startTime={},endTime={},callId={},recordId={}",
                app, stream, mediaServerId, startTime, endTime, callId, recordId);
        if (recordId != null) {
            return cloudRecordService.changeCollectById(recordId, true);
        }else {
            return cloudRecordService.changeCollect(true, app, stream, mediaServerId, startTime, endTime, callId);
        }
    }

    @ResponseBody
    @GetMapping("/collect/delete")
    @Operation(summary = "移除收藏")
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "startTime", description = "鉴权ID", required = false)
    @Parameter(name = "endTime", description = "鉴权ID", required = false)
    @Parameter(name = "callId", description = "鉴权ID", required = false)
    @Parameter(name = "recordId", description = "录像记录的ID，用于精准精准移除一个视频文件的收藏", required = false)
    public int deleteCollect(
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream,
            @RequestParam(required = false) String mediaServerId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String callId,
            @RequestParam(required = false) Integer recordId
    ){
        logger.info("[云端录像] 移除收藏，app={}，stream={},mediaServerId={},startTime={},endTime={},callId={},recordId={}",
                app, stream, mediaServerId, startTime, endTime, callId, recordId);
        if (recordId != null) {
            return cloudRecordService.changeCollectById(recordId, false);
        }else {
            return cloudRecordService.changeCollect(false, app, stream, mediaServerId, startTime, endTime, callId);
        }
    }

    @ResponseBody
    @GetMapping("/play/path")
    @Operation(summary = "获取播放地址")
    @Parameter(name = "recordId", description = "录像记录的ID", required = true)
    public DownloadFileInfo getPlayUrlPath(
            @RequestParam(required = true) Integer recordId
    ){
        return cloudRecordService.getPlayUrlPath(recordId);
    }
}
