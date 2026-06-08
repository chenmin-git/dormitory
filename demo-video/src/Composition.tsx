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
import scenes from "../public/full-demo-scenes.json";
import captions from "../public/full-demo-captions.json";

type Scene = {
  id: string;
  title: string;
  subtitle: string;
  screenshot: string;
  bullets: string[];
  duration: number;
  accent: string;
};

const typedScenes = scenes as Scene[];
const typedCaptions = captions as Record<string, string>;

const sceneStartFrame = (index: number, fps: number) =>
  typedScenes.slice(0, index).reduce((sum, scene) => sum + scene.duration * fps, 0);

const SceneCard: React.FC<{ scene: Scene; index: number }> = ({ scene, index }) => {
  const frame = useCurrentFrame();
  const localFrame = frame;
  const enter = interpolate(localFrame, [0, 18], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.bezier(0.16, 1, 0.3, 1),
  });
  const imageY = interpolate(localFrame, [0, 24], [18, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.bezier(0.16, 1, 0.3, 1),
  });

  return (
    <AbsoluteFill className="scene" style={{ ["--accent" as string]: scene.accent }}>
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">宿</div>
          <div>
            <div className="brand-title">智能宿舍管理系统</div>
            <div className="brand-subtitle">SpringBoot + Vue3 + AI Agent</div>
          </div>
        </div>
        <div className="scene-count">
          {String(index + 1).padStart(2, "0")} / {String(typedScenes.length).padStart(2, "0")}
        </div>
      </div>

      <div className="content">
        <div className="copy" style={{ opacity: enter, transform: `translateY(${(1 - enter) * 16}px)` }}>
          <div className="eyebrow">{typedCaptions[scene.id]}</div>
          <h1>{scene.title}</h1>
          <p>{scene.subtitle}</p>
          <div className="bullets">
            {scene.bullets.map((bullet) => (
              <div className="bullet" key={bullet}>
                <span />
                {bullet}
              </div>
            ))}
          </div>
        </div>

        <div className="shot-wrap" style={{ opacity: enter, transform: `translateY(${imageY}px)` }}>
          <Img src={staticFile(`screenshots/${scene.screenshot}`)} className="shot" />
        </div>
      </div>

      <div className="footer-line" />
    </AbsoluteFill>
  );
};

export const MyComposition = () => {
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
    </AbsoluteFill>
  );
};

export const totalDurationInFrames = typedScenes.reduce((sum, scene) => sum + scene.duration * 30, 0);
