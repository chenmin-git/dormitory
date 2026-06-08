import "./index.css";
import { Composition } from "remotion";
import { MyComposition, totalDurationInFrames } from "./Composition";

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="MyComp"
        component={MyComposition}
        durationInFrames={totalDurationInFrames}
        fps={30}
        width={1920}
        height={1080}
      />
    </>
  );
};
