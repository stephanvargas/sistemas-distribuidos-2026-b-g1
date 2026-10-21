export class StructuredLogger {
  constructor(serviceName = 'educk-attendance-api', version = '2.0.0', env = 'qa') {
    this.service = serviceName;
    this.version = version;
    this.env = env;
  }

  log(level, event, message, ctx = {}, domain = {}) {
    const entry = {
      timestamp: new Date().toISOString(),
      level: level.toUpperCase(),
      service: this.service,
      version: this.version,
      environment: this.env,
      trace_id: ctx.traceId || null,
      span_id: ctx.spanId || null,
      correlation_id: ctx.correlationId || domain.correlation_id || null,
      event,
      message,
      duration_ms: domain.duration_ms ?? 0,
      student_id: domain.student_id || null,
      session_id: domain.session_id || null,
      course_id: domain.course_id || null,
      attendance_status: domain.attendance_status || null
    };
    const jsonString = JSON.stringify(entry);
    if (level === 'ERROR') {
      console.error(jsonString);
    } else {
      console.log(jsonString);
    }
    return entry;
  }

  info(event, message, ctx, domain) { return this.log('INFO', event, message, ctx, domain); }
  warn(event, message, ctx, domain) { return this.log('WARN', event, message, ctx, domain); }
  error(event, message, ctx, domain) { return this.log('ERROR', event, message, ctx, domain); }
}
