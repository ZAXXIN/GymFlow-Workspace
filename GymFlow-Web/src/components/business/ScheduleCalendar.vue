<template>
  <div class="schedule-calendar">
    <!-- 日历头部 -->
    <div class="calendar-header">
      <div class="header-left">
        <h2 class="calendar-title">{{ calendarTitle }}</h2>
        <el-tag type="info" size="small">{{ currentView }}视图</el-tag>
      </div>
      
      <div class="header-center">
        <el-button-group>
          <el-button @click="previousPeriod">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button @click="today">{{ todayText }}</el-button>
          <el-button @click="nextPeriod">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
        
        <div class="current-period">{{ currentPeriodText }}</div>
      </div>
      
      <div class="header-right">
        <el-button-group>
          <el-button
            :type="currentView === 'day' ? 'primary' : ''"
            @click="switchView('day')"
          >
            日
          </el-button>
          <el-button
            :type="currentView === 'week' ? 'primary' : ''"
            @click="switchView('week')"
          >
            周
          </el-button>
          <el-button
            :type="currentView === 'month' ? 'primary' : ''"
            @click="switchView('month')"
          >
            月
          </el-button>
        </el-button-group>
        
        <el-button
          type="primary"
          @click="handleCreateSchedule"
          style="margin-left: 10px;"
        >
          新建排班
        </el-button>
      </div>
    </div>
    
    <!-- 日历视图切换 -->
    <div class="calendar-view">
      <!-- 月视图 -->
      <div v-if="currentView === 'month'" class="month-view">
        <div class="month-grid">
          <!-- 星期标题 -->
          <div class="weekdays">
            <div class="weekday" v-for="day in weekdays" :key="day">
              {{ day }}
            </div>
          </div>
          
          <!-- 日期格子 -->
          <div class="month-days">
            <div
              v-for="day in monthDays"
              :key="day.date"
              class="day-cell"
              :