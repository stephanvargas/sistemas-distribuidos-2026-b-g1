import test from 'node:test';
import assert from 'node:assert/strict';
import { parseTraceparent } from './trace.js';
import { traceContextMiddleware } from './middleware.js';
import { StructuredLogger } from './logger.js';
import { MetricsRegistry } from './metrics.js';
import { forwardDownstreamCall } from './downstream.js';

test('Attendance Observability - trace context extraction and propagation', async (t) => {
  const incomingTraceId = '7c10b4288e224e73b2901a18bc0094ef';
  const incomingParentSpan = '11a056bb0ca103c8';
  const rawTraceparent = '00-' + incomingTraceId + '-' + incomingParentSpan + '-01';

  const req = { headers: { 'traceparent': rawTraceparent } };
  const resHeaders = {};
  const res = { setHeader: (k, v) => { resHeaders[k.toLowerCase()] = v; } };

  traceContextMiddleware(req, res);

  assert.equal(req.traceCtx.traceId, incomingTraceId);
  assert.equal(req.traceCtx.parentSpanId, incomingParentSpan);
  assert.ok(resHeaders['traceparent']);

  const downstreamCall = await forwardDownstreamCall(req.traceCtx, {
    studentId: 's-8812-diego-rojas',
    status: 'ABSENT'
  });

  const parsedDownstream = parseTraceparent(downstreamCall.headers['traceparent']);
  assert.equal(parsedDownstream.traceId, incomingTraceId, 'Downstream AMQP call must keep exact trace_id');
});

test('Attendance Observability - structured logger output matches schema', () => {
  const logger = new StructuredLogger('educk-attendance-api', '2.0.0', 'qa');
  const traceCtx = { traceId: '7c10b4288e224e73b2901a18bc0094ef', spanId: '6d95b97c4e88a023' };
  const entry = logger.info('attendance.recorded', 'Student attendance saved', traceCtx, {
    student_id: 's-8812-diego-rojas',
    duration_ms: 18
  });

  assert.equal(entry.trace_id, traceCtx.traceId);
  assert.equal(entry.service, 'educk-attendance-api');
});

test('Attendance Observability - metrics registry exposes Prometheus output', () => {
  const metrics = new MetricsRegistry();
  metrics.incRequest('success');
  metrics.observeDuration(0.038);

  const output = metrics.formatPrometheus();
  assert.match(output, /attendance_records_total\{outcome="success"\} 1/);
  assert.match(output, /attendance_duration_seconds_bucket\{le="0.05"\} 1/);
});
