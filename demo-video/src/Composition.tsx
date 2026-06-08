import type { Caption } from "@remotion/captions";
import type { CSSProperties, FC } from "react";
import {
  AbsoluteFill,
  Easing,
  Img,
  interpolate,
  Sequence,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import captions from "../public/full-demo-captions.json";
import scenes from "../public/full-demo-scenes.json";

type Scene = {
  id: string;
  title: string;
  subtitle: string;
  screenshot: string;
  duration: number;
  accent: string;
  kicker: string;
  bullets: string[];
};

const FRAME_RATE = 30;
const typedScenes = scenes as Scene[];
const typedCaptions = captions as Caption[];

const easeOut = Easing.bezier(0.16, 1, 0.3, 1);

const secondsBeforeScene = (index: number) =>
  typedScenes.slice(0, index).reduce((sum, scene) => sum + scene.duration, 0);

const sceneStartFrame = (index: number, fps: number) => secondsBeforeScene(index) * fps;

const activeSceneForMs = (timeMs: number) => {
  const elapsedSeconds = timeMs / 1000;
  let cursor = 0;
  for (const scene of typedScenes) {
    const next = cursor + scene.duration;
    if (elapsedSeconds >= cursor && elapsedSeconds < next) {
      return scene;
    }
    cursor = next;
  }
  return typedScenes[typedScenes.length - 1];
};

const clampInterpolate = (
  value: number,
  input: [number, number] | [number, number, number, number],
  output: [number, number] | [number, number, number, number],
) =>
  interpolate(value, input, output, {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: easeOut,
  });

const SceneCard: FC<{ scene: Scene; index: number }> = ({ scene, index }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const sceneFrames = scene.duration * fps;
  const enter = clampInterpolate(frame, [0, 20], [0, 1]);
  const exit = clampInterpolate(frame, [sceneFrames - 14, sceneFrames], [1, 0]);
  const opacity = enter * exit;
  const shotX = clampInterpolate(frame, [0, 24], [42, 0]);
  const panelX = clampInterpolate(frame, [0, 22], [-34, 0]);
  const zoom = interpolate(frame, [0, sceneFrames], [1.01, 1.028], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const progress = interpolate(frame, [0, sceneFrames], [0, 100], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const style = { "--accent": scene.accent } as CSSProperties;

  return (
    <AbsoluteFill className="scene" style={style}>
      <Img
        src={staticFile(`screenshots/${scene.screenshot}`)}
        className="ambient-shot"
        style={{ transform: `scale(${1.08 + (zoom - 1) * 0.8})` }}
      />
      <div className="scene-vignette" />

      <div
        className="shot-wrap"
        style={{
          opacity,
          transform: `translateX(${shotX}px) scale(${zoom})`,
        }}
      >
        <Img src={staticFile(`screenshots/${scene.screenshot}`)} className="shot" />
      </div>

      <div
        className="copy-panel"
        style={{
          opacity,
          transform: `translateX(${panelX}px)`,
        }}
      >
        <div className="brand-row">
          <div className="brand-mark">宿</div>
          <div>
            <div className="brand-title">智能宿舍</div>
            <div className="brand-subtitle">DORMITORY AI</div>
          </div>
        </div>

        <div className="kicker">{scene.kicker}</div>
        <h1>{scene.title}</h1>
        <p>{scene.subtitle}</p>

        <div className="bullets">
          {scene.bullets.map((bullet, bulletIndex) => {
            const bulletIn = clampInterpolate(frame, [10 + bulletIndex * 4, 24 + bulletIndex * 4], [0, 1]);
            return (
              <div
                className="bullet"
                key={bullet}
                style={{
                  opacity: bulletIn,
                  transform: `translateY(${(1 - bulletIn) * 10}px)`,
                }}
              >
                <span />
                {bullet}
              </div>
            );
          })}
        </div>

        <div className="panel-progress">
          <div style={{ width: `${progress}%` }} />
        </div>
      </div>

      <div className="scene-count" style={{ opacity }}>
        {String(index + 1).padStart(2, "0")}
        <span>/</span>
        {String(typedScenes.length).padStart(2, "0")}
      </div>
    </AbsoluteFill>
  );
};

const CaptionOverlay: FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const nowMs = (frame / fps) * 1000;
  const caption =
    typedCaptions.find((item) => nowMs >= item.startMs && nowMs < item.endMs) ??
    typedCaptions[typedCaptions.length - 1];
  const scene = activeSceneForMs(nowMs);
  const localMs = Math.max(0, nowMs - caption.startMs);
  const durationMs = Math.max(1, caption.endMs - caption.startMs);
  const enter = clampInterpolate(localMs, [0, 360], [0, 1]);
  const exit = clampInterpolate(localMs, [durationMs - 360, durationMs], [1, 0]);
  const opacity = enter * exit;
  const y = interpolate(localMs, [0, 360, durationMs - 360, durationMs], [28, 0, 0, 16], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: easeOut,
  });
  const progress = interpolate(localMs, [0, durationMs], [0, 100], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const chars = Array.from(caption.text);
  const reveal = interpolate(localMs, [180, durationMs - 620], [0, chars.length], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.bezier(0.22, 1, 0.36, 1),
  });
  const activeIndex = Math.floor(reveal);

  return (
    <div
      className="caption-wrap"
      style={{
        "--accent": scene.accent,
        opacity,
        transform: `translateY(${y}px)`,
      } as CSSProperties}
    >
      <div className="caption-label">
        <span />
        实时字幕
      </div>
      <div className="caption-text">
        {chars.map((char, index) => {
          const shown = index < reveal;
          const active = index === activeIndex && char.trim() !== "";
          return (
            <span
              key={`${caption.startMs}-${index}`}
              style={{
                color: active ? scene.accent : shown ? "#ffffff" : "rgba(255, 255, 255, 0.34)",
                opacity: shown ? 1 : 0.74,
                transform: active ? "translateY(-2px)" : "translateY(0)",
              }}
            >
              {char}
            </span>
          );
        })}
      </div>
      <div className="caption-progress">
        <div style={{ width: `${progress}%` }} />
      </div>
    </div>
  );
};

export const MyComposition: FC = () => {
  const { fps } = useVideoConfig();

  return (
    <AbsoluteFill className="video-root">
      {typedScenes.map((scene, index) => (
        <Sequence
          key={scene.id}
          from={sceneStartFrame(index, fps)}
          durationInFrames={scene.duration * fps}
        >
          <SceneCard scene={scene} index={index} />
        </Sequence>
      ))}
      <CaptionOverlay />
    </AbsoluteFill>
  );
};

export const totalDurationInFrames = typedScenes.reduce(
  (sum, scene) => sum + scene.duration * FRAME_RATE,
  0,
);
