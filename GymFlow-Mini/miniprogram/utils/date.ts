// utils/date.wxs
function formatTime(time) {
  if (!time) return '';
  return time.substring(0, 5);
}

function formatDate(date) {
  if (!date) return '';
  return date;
}

function isToday(dateStr) {
  if (!dateStr) return false;
  var today = getDate();
  var year = today.getFullYear();
  var month = today.getMonth() + 1;
  var day = today.getDate();
  var date = getDate(dateStr);
  return date.getFullYear() === year && date.getMonth() + 1 === month && date.getDate() === day;
}

function isTomorrow(dateStr) {
  if (!dateStr) return false;
  var today = getDate();
  var tomorrow = getDate();
  tomorrow.setDate(today.getDate() + 1);
  var date = getDate(dateStr);
  return date.getFullYear() === tomorrow.getFullYear() && date.getMonth() + 1 === tomorrow.getMonth() + 1 && date.getDate() === tomorrow.getDate();
}

function getDateDisplay(dateStr) {
  if (!dateStr) return '';
  if (isToday(dateStr)) {
    return '今天';
  } else if (isTomorrow(dateStr)) {
    return '明天';
  } else {
    var date = getDate(dateStr);
    var month = date.getMonth() + 1;
    var day = date.getDate();
    return month + '月' + day + '日';
  }
}

function getCourseStatus(course) {
  if (!course || !course.scheduleDate || !course.startTime || !course.endTime) {
    return {
      'class': '',
      'text': ''
    };
  }
  
  var now = getDate();
  var scheduleDate = getDate(course.scheduleDate + ' ' + course.startTime);
  var courseEnd = getDate(course.scheduleDate + ' ' + course.endTime);
  
  if (now < scheduleDate) {
    return {
      'class': 'upcoming',
      'text': '待上课'
    };
  } else if (now > courseEnd) {
    return {
      'class': 'completed',
      'text': '已结束'
    };
  } else {
    return {
      'class': 'ongoing',
      'text': '进行中'
    };
  }
}

function getBookingStatusText(status) {
  var statusMap = {
    '0': '待上课',
    '1': '已签到',
    '2': '已完成',
    '3': '已取消',
    '4': '已过期'
  };
  return statusMap[status] || '未知';
}

function getBookingStatusClass(status) {
  var classMap = {
    '0': 'pending',
    '1': 'checked',
    '2': 'completed',
    '3': 'cancelled',
    '4': 'expired'
  };
  return classMap[status] || 'unknown';
}

function getRemainingText(remaining) {
  if (remaining <= 0) return '已满';
  if (remaining <= 5) return '仅剩' + remaining + '席';
  return remaining + '席';
}

function getRemainingClass(remaining) {
  if (remaining <= 0) return 'full';
  if (remaining <= 5) return 'low';
  return 'normal';
}

function getCourseTypeText(type) {
  if (type === 0) {
    return '私教';
  } else {
    return '团课';
  }
}

function getCourseTypeClass(type) {
  if (type === 0) {
    return 'private';
  } else {
    return 'group';
  }
}

function formatMoney(amount) {
  if (!amount && amount !== 0) return '¥0.00';
  return '¥' + amount.toFixed(2);
}

function getGenderText(gender) {
  if (gender === 0) return '女';
  if (gender === 1) return '男';
  return '未知';
}

module.exports["formatTime"] = formatTime;
module.exports["formatDate"] = formatDate;
module.exports["isToday"] = isToday;
module.exports["isTomorrow"] = isTomorrow;
module.exports["getDateDisplay"] = getDateDisplay;
module.exports["getCourseStatus"] = getCourseStatus;
module.exports["getBookingStatusText"] = getBookingStatusText;
module.exports["getBookingStatusClass"] = getBookingStatusClass;
module.exports["getRemainingText"] = getRemainingText;
module.exports["getRemainingClass"] = getRemainingClass;
module.exports["getCourseTypeText"] = getCourseTypeText;
module.exports["getCourseTypeClass"] = getCourseTypeClass;
module.exports["formatMoney"] = formatMoney;
module.exports["getGenderText"] = getGenderText;