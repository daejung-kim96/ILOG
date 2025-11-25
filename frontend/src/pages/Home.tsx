// pages/Home.jsx
import Album from "./album/Album";
import OurEye from "./oureye/OurEye";
import Report from "./report/Report";

const Home = () => {
  return (
    <div>
      {/* <Diary /> */}
      <Album isPreview={true} maxItems={2} />
      <OurEye />
      <Report />
    </div>
  );
};

export default Home;
